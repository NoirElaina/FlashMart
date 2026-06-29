package org.example.flashmart.product.service;

import org.example.flashmart.product.model.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> listEnabled();
}
