package org.example.flashmart.product.controller;

import org.example.flashmart.common.response.Result;
import org.example.flashmart.product.model.vo.CategoryVO;
import org.example.flashmart.product.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.listEnabled());
    }
}
