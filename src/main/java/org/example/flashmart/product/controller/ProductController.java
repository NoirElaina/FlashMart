package org.example.flashmart.product.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.common.response.PageResult;
import org.example.flashmart.common.response.Result;
import org.example.flashmart.product.model.query.ProductQuery;
import org.example.flashmart.product.model.vo.ProductDetailVO;
import org.example.flashmart.product.model.vo.ProductVO;
import org.example.flashmart.product.service.ProductDetailService;
import org.example.flashmart.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductDetailService  productDetailService;

    @GetMapping
    public Result<PageResult<ProductVO>> pageProducts(@Valid ProductQuery productQuery) {
        return Result.success(productService.pageProducts(productQuery));
    }

    @GetMapping("/{productId}")
    public Result<ProductDetailVO> productDetail(@PathVariable @Positive(message = "商品 ID 不合法") Long productId) {
        ProductDetailVO productDetailVO = productDetailService.productDetail(productId);
        return Result.success(productDetailVO);
    }
}
