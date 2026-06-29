package org.example.flashmart.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.flashmart.product.model.vo.CategoryVO;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name FROM category WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<CategoryVO> selectEnabled();
}
