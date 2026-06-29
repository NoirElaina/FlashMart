package org.example.flashmart.product.service;

import org.example.flashmart.common.response.PageResult;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.example.flashmart.product.model.query.ProductQuery;
import org.example.flashmart.product.model.vo.ProductVO;

import java.math.BigDecimal;

public interface ProductService {
    PageResult<ProductVO> pageProducts(ProductQuery productQuery);

    ProductDO getById(Long productId);

    void updatePrice(Long productId, BigDecimal originalPrice, BigDecimal salePrice);

    void updateStatus(Long productId, Integer status);
}
