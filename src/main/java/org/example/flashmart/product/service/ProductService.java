package org.example.flashmart.product.service;

import org.example.flashmart.common.response.PageResult;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.example.flashmart.product.model.query.ProductQuery;

public interface ProductService {
    PageResult<ProductDO> pageProducts(ProductQuery productQuery);

    ProductDO getById(Long productId);
}
