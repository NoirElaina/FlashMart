package org.example.flashmart.cart.service.impl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.flashmart.cart.cache.CartCacheService;
import org.example.flashmart.cart.mapper.CartMapper;
import org.example.flashmart.cart.model.dto.CartBatchDeleteDTO;
import org.example.flashmart.cart.model.dto.CartItemAddDTO;
import org.example.flashmart.cart.model.dataobject.ShoppingCartDO;
import org.example.flashmart.cart.model.dto.CartItemUpdateDTO;
import org.example.flashmart.cart.model.dto.CartSelectedBatchUpdateDTO;
import org.example.flashmart.cart.model.queryobject.CartItemQueryObject;
import org.example.flashmart.cart.model.vo.CartVO;
import org.example.flashmart.cart.service.CartService;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.product.mapper.ProductMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartCacheService cartCacheService;


    @Override
    public List<CartVO> getCartItems(Long userId) {
        return cartMapper.selectCartItemsByUserId(userId)
                .stream()
                .map(this::toCartVO)
                .collect(Collectors.toList());
    }

    @Override
    public void addToCart(Long userId, CartItemAddDTO cartItemAddDTO) {
        Integer quantity = cartItemAddDTO.getQuantity() == null || cartItemAddDTO.getQuantity() <= 0
                ? 1
                : cartItemAddDTO.getQuantity();

        ProductDO productDO=productMapper.selectById(Long.valueOf(cartItemAddDTO.getProductId()));

        if (productDO==null) {
            throw new BusinessException(404,"商品不存在");
        }
        if (productDO.getStatus() == null || productDO.getStatus() != 1) {
            throw new BusinessException("商品已下架，无法加入购物车");
        }

        ShoppingCartDO existingCartItem = cartMapper.selectByUserIdAndProductId(userId, cartItemAddDTO.getProductId());
        if (existingCartItem != null) {
            int mergeQuantity = existingCartItem.getQuantity() + quantity;
            validateQuantity(productDO, mergeQuantity);
            cartMapper.updateQuantityById(existingCartItem.getId(), mergeQuantity);
            cartCacheService.evictCartCount(userId);
            return;
        }

        validateQuantity(productDO, quantity);
        ShoppingCartDO shoppingCartDO = new ShoppingCartDO();
        shoppingCartDO.setUserId(userId.intValue());
        shoppingCartDO.setProductId(cartItemAddDTO.getProductId());
        shoppingCartDO.setQuantity(quantity);
        shoppingCartDO.setSelected(1);
        cartMapper.insert(shoppingCartDO);
        cartCacheService.evictCartCount(userId);
    }

    @Override
    public void removeCartItem(Long userId, Integer cartId) {
        int deleted = cartMapper.deleteByIdAndUserId(userId, cartId);
        if (deleted <= 0) {
            throw new BusinessException(404, "购物车商品不存在");
        }
        cartCacheService.evictCartCount(userId);
    }

    @Override
    public void updateCart(Long userId, @Positive Integer cartId, @Valid CartItemUpdateDTO cartItemUpdateDTO) {
        ShoppingCartDO shoppingCartDO = cartMapper.selectByIdAndUserId(cartId, userId);
        if (shoppingCartDO == null) {
            throw new BusinessException(404, "购物车商品不存在");
        }

        ProductDO productDO = productMapper.selectById(Long.valueOf(shoppingCartDO.getProductId()));
        if (productDO == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (productDO.getStatus() == null || productDO.getStatus() != 1) {
            throw new BusinessException("商品已下架，无法更新购物车");
        }

        if (cartItemUpdateDTO.getQuantity() != null) {
            validateQuantity(productDO, cartItemUpdateDTO.getQuantity());
        }

        cartMapper.updateCart(userId, cartId, cartItemUpdateDTO);
        cartCacheService.evictCartCount(userId);
    }

    @Override
    public Integer countCartItems(Long userId) {
        Integer cachedCount = cartCacheService.getCartCount(userId);
        if (cachedCount != null) {
            return cachedCount;
        }

        Integer count = cartMapper.countQuantityByUserId(userId);
        Integer safeCount = count == null ? 0 : count;
        cartCacheService.putCartCount(userId, safeCount);
        return safeCount;
    }

    @Override
    public void updateSelectedBatch(Long userId, CartSelectedBatchUpdateDTO dto) {
        Integer selected = dto.getSelected();
        if (selected == null || (selected != 0 && selected != 1)) {
            throw new BusinessException("勾选状态不合法");
        }

        int updated = cartMapper.updateSelectedBatch(userId, dto.getCartIds(), selected);
        if (updated <= 0) {
            throw new BusinessException(404, "购物车商品不存在");
        }
        cartCacheService.evictCartCount(userId);
    }

    @Override
    public void removeCartItems(Long userId, CartBatchDeleteDTO dto) {
        int deleted = cartMapper.deleteBatchByUserId(userId, dto.getCartIds());
        if (deleted <= 0) {
            throw new BusinessException(404, "购物车商品不存在");
        }
        cartCacheService.evictCartCount(userId);
    }

    @Override
    public void removeInvalidItems(Long userId) {
        // 清理已下架/已删除的商品；清理 0 条也算成功（幂等）。
        int deleted = cartMapper.deleteInvalidByUserId(userId);
        if (deleted > 0) {
            cartCacheService.evictCartCount(userId);
        }
    }

    private void validateQuantity(ProductDO productDO, Integer quantity) {
        if (productDO.getStock() == null || productDO.getStock() <= 0) {
            throw new BusinessException("商品库存不足");
        }
        if (quantity > productDO.getStock()) {
            throw new BusinessException("商品数量不能超过库存");
        }
        if (productDO.getLimitPerUser() != null && productDO.getLimitPerUser() > 0
                && quantity > productDO.getLimitPerUser()) {
            throw new BusinessException("商品数量不能超过限购数量");
        }
    }

    private CartVO toCartVO(CartItemQueryObject cartItemQueryObject) {
        CartVO cartVO = new CartVO();
        cartVO.setId(cartItemQueryObject.getId());
        cartVO.setProductId(cartItemQueryObject.getProductId());
        cartVO.setName(cartItemQueryObject.getName());
        cartVO.setImage(cartItemQueryObject.getImage());
        cartVO.setCategory(cartItemQueryObject.getCategory());
        cartVO.setOriginalPrice(cartItemQueryObject.getOriginalPrice());
        cartVO.setSalePrice(cartItemQueryObject.getSalePrice());
        cartVO.setQuantity(cartItemQueryObject.getQuantity());
        cartVO.setSelected(cartItemQueryObject.getSelected());
        cartVO.setStock(cartItemQueryObject.getStock());
        cartVO.setSold(cartItemQueryObject.getSold());
        cartVO.setLimitPerUser(cartItemQueryObject.getLimitPerUser());
        // 商品被删除(左连接查不到 name) 或已下架(status != 1) 视为失效项，前端可标灰并提示清理。
        boolean deleted = cartItemQueryObject.getName() == null;
        boolean offShelf = !deleted
                && (cartItemQueryObject.getStatus() == null || cartItemQueryObject.getStatus() != 1);
        cartVO.setAvailable(!deleted && !offShelf);
        cartVO.setInvalidReason(deleted ? "商品不存在" : (offShelf ? "商品已下架" : null));
        cartVO.setCreateTime(cartItemQueryObject.getCreateTime());
        cartVO.setUpdateTime(cartItemQueryObject.getUpdateTime());
        return cartVO;
    }
}
