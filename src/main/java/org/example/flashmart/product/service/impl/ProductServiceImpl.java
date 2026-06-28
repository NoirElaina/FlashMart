package org.example.flashmart.product.service.impl;

import org.example.flashmart.common.response.PageResult;
import org.example.flashmart.product.mapper.ProductMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.example.flashmart.product.model.query.ProductQuery;
import org.example.flashmart.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<ProductDO> pageProducts(ProductQuery productQuery) {
        int currentPage = (productQuery.getPage() == null || productQuery.getPage() < 1) ? 1 : productQuery.getPage();
        int size = (productQuery.getPageSize() == null || productQuery.getPageSize() < 1) ? 10 : productQuery.getPageSize();
        int offset = (currentPage - 1) * size;
        String category = productQuery.getCategory();
        String normalizedCategory = (category == null || category.isBlank() || "全部".equals(category)) ? null : category;
        List<ProductDO> products = productMapper.pageProducts(offset, size, normalizedCategory);
        long total = productMapper.countProducts(normalizedCategory);
        return new PageResult<>(products, total, currentPage, size);
    }

    @Override
    public ProductDO getById(Long productId) {
        return productMapper.selectById(productId);
    }

}
