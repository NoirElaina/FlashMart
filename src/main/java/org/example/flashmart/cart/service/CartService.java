package org.example.flashmart.cart.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.flashmart.cart.model.dto.CartBatchDeleteDTO;
import org.example.flashmart.cart.model.dto.CartItemAddDTO;
import org.example.flashmart.cart.model.dto.CartItemUpdateDTO;
import org.example.flashmart.cart.model.dto.CartSelectedBatchUpdateDTO;
import org.example.flashmart.cart.model.vo.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> getCartItems(Long userId);

    void addToCart(Long userId, CartItemAddDTO cartItemAddDTO);

    void removeCartItem(Long userId, Integer cartId);

    void updateCart(Long userId, @Positive Integer cartId, @Valid CartItemUpdateDTO cartItemUpdateDTO);

    Integer countCartItems(Long userId);

    void updateSelectedBatch(Long userId, CartSelectedBatchUpdateDTO dto);

    void removeCartItems(Long userId, CartBatchDeleteDTO dto);

    void removeInvalidItems(Long userId);
}
