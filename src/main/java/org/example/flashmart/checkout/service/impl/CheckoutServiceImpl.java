package org.example.flashmart.checkout.service.impl;

import org.example.flashmart.cart.mapper.CartMapper;
import org.example.flashmart.cart.model.queryobject.CartItemQueryObject;
import org.example.flashmart.checkout.model.dto.CheckoutPreviewDTO;
import org.example.flashmart.checkout.model.vo.CheckoutItemVO;
import org.example.flashmart.checkout.model.vo.CheckoutPreviewVO;
import org.example.flashmart.checkout.service.CheckoutService;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.product.mapper.ProductMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CheckoutServiceImpl(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    @Override
    public CheckoutPreviewVO preview(Long userId, CheckoutPreviewDTO dto) {
        String mode = normalizeMode(dto.getMode());
        // 购物车结算和立即购买共用预览模型，但来源校验必须分开处理。
        List<CheckoutItemVO> items = switch (mode) {
            case "BUY_NOW" -> previewBuyNow(dto);
            case "SECKILL" -> previewSeckill(dto);
            default -> previewCart(userId, dto);
        };

        return buildPreview(items);
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new BusinessException("结算来源不能为空");
        }

        String normalized = mode.trim();
        if ("CART".equals(normalized) || "BUY_NOW".equals(normalized) || "SECKILL".equals(normalized)) {
            return normalized;
        }

        throw new BusinessException("结算来源不合法");
    }

    private List<CheckoutItemVO> previewCart(Long userId, CheckoutPreviewDTO dto) {
        if (dto.getCartIds() == null || dto.getCartIds().isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        List<CartItemQueryObject> cartItems = cartMapper.selectCartItemsByIds(userId, dto.getCartIds());
        if (cartItems.isEmpty()) {
            throw new BusinessException(404, "结算商品不存在");
        }
        if (cartItems.size() != dto.getCartIds().size()) {
            throw new BusinessException("部分结算商品不存在");
        }

        return cartItems.stream()
                .map(this::toCheckoutItemVO)
                .toList();
    }

    private List<CheckoutItemVO> previewBuyNow(CheckoutPreviewDTO dto) {
        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new BusinessException("请选择要购买的商品");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("购买数量不合法");
        }

        ProductDO product = productMapper.selectById(Long.valueOf(dto.getProductId()));
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getLimitPerUser() != null && dto.getQuantity() > product.getLimitPerUser()) {
            throw new BusinessException("超过商品限购数量");
        }

        // 立即购买只生成临时结算项，不写入 shopping_cart。
        return List.of(toCheckoutItemVO(product, dto.getQuantity()));
    }

    private List<CheckoutItemVO> previewSeckill(CheckoutPreviewDTO dto) {
        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new BusinessException("请选择要秒杀的商品");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("购买数量不合法");
        }

        ProductDO product = productMapper.selectById(Long.valueOf(dto.getProductId()));
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!Boolean.TRUE.equals(product.getSeckill())) {
            throw new BusinessException("当前商品不是秒杀商品");
        }

        LocalDateTime now = LocalDateTime.now();
        if (product.getSeckillStartTime() == null || now.isBefore(product.getSeckillStartTime())) {
            throw new BusinessException("秒杀尚未开始");
        }
        if (product.getSeckillEndTime() == null || now.isAfter(product.getSeckillEndTime())) {
            throw new BusinessException("秒杀已结束");
        }
        if (product.getLimitPerUser() != null && dto.getQuantity() > product.getLimitPerUser()) {
            throw new BusinessException("超过商品限购数量");
        }

        return List.of(toCheckoutItemVO(product, dto.getQuantity()));
    }

    private CheckoutPreviewVO buildPreview(List<CheckoutItemVO> items) {
        // 金额统一在这里汇总，保证 CART 和 BUY_NOW 两条链路口径一致。
        int totalQuantity = items.stream()
                .mapToInt(CheckoutItemVO::getQuantity)
                .sum();
        BigDecimal productAmount = items.stream()
                .map(CheckoutItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = productAmount.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ZERO : new BigDecimal("18.00");
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean canSubmit = items.stream().allMatch(CheckoutItemVO::getAvailable);

        CheckoutPreviewVO vo = new CheckoutPreviewVO();
        vo.setItems(items);
        vo.setTotalQuantity(totalQuantity);
        vo.setProductAmount(productAmount);
        vo.setShippingFee(shippingFee);
        vo.setDiscountAmount(discountAmount);
        vo.setPayableAmount(productAmount.add(shippingFee).subtract(discountAmount));
        vo.setCanSubmit(canSubmit);
        return vo;
    }

    private CheckoutItemVO toCheckoutItemVO(CartItemQueryObject item) {
        // 小计、可提交状态属于业务派生字段，显式组装比自动拷贝更安全。
        boolean available = item.getStock() != null && item.getStock() >= item.getQuantity();
        String reason = available ? null : "商品库存不足";

        CheckoutItemVO vo = new CheckoutItemVO();
        vo.setCartId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getName());
        vo.setProductImage(item.getImage());
        vo.setSalePrice(item.getSalePrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        vo.setStock(item.getStock());
        vo.setAvailable(available);
        vo.setUnavailableReason(reason);
        return vo;
    }

    private CheckoutItemVO toCheckoutItemVO(ProductDO product, Integer quantity) {
        boolean online = Integer.valueOf(1).equals(product.getStatus());
        boolean stockEnough = product.getStock() != null && product.getStock() >= quantity;
        boolean available = online && stockEnough;
        String reason = null;
        if (!online) {
            reason = "商品已下架";
        } else if (!stockEnough) {
            reason = "商品库存不足";
        }

        CheckoutItemVO vo = new CheckoutItemVO();
        vo.setCartId(null);
        vo.setProductId(product.getId());
        vo.setProductName(product.getName());
        vo.setProductImage(product.getImage());
        vo.setSalePrice(product.getSalePrice());
        vo.setQuantity(quantity);
        vo.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(quantity)));
        vo.setStock(product.getStock());
        vo.setAvailable(available);
        vo.setUnavailableReason(reason);
        return vo;
    }
}
