package org.example.flashmart.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.common.response.ResultCode;
import org.example.flashmart.common.response.Result;
import org.example.flashmart.product.model.dto.ProductPriceUpdateDTO;
import org.example.flashmart.product.service.ProductService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{productId}/price")
    public Result<String> updatePrice(@PathVariable @Positive Long productId,
                                      @Valid @RequestBody ProductPriceUpdateDTO dto,
                                      @RequestAttribute String role) {
        requireAdmin(role);
        productService.updatePrice(productId, dto.getOriginalPrice(), dto.getSalePrice());
        return Result.success("改价成功");
    }

    @PutMapping("/{productId}/status")
    public Result<String> updateStatus(@PathVariable @Positive Long productId,
                                       @RequestParam Integer status,
                                       @RequestAttribute String role) {
        requireAdmin(role);
        productService.updateStatus(productId, status);
        return Result.success("更新上下架状态成功");
    }

    private void requireAdmin(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "需要管理员权限");
        }
    }
}
