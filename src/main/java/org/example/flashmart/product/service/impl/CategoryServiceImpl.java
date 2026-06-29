package org.example.flashmart.product.service.impl;

import org.example.flashmart.product.mapper.CategoryMapper;
import org.example.flashmart.product.model.vo.CategoryVO;
import org.example.flashmart.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryVO> listEnabled() {
        return categoryMapper.selectEnabled();
    }
}
