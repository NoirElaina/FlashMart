package org.example.flashmart.cart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.flashmart.cart.model.dto.CartBatchDeleteDTO;
import org.example.flashmart.cart.model.dto.CartItemAddDTO;
import org.example.flashmart.cart.model.dto.CartItemUpdateDTO;
import org.example.flashmart.cart.model.dto.CartSelectedBatchUpdateDTO;
import org.example.flashmart.cart.model.vo.CartVO;
import org.example.flashmart.cart.service.CartService;
import org.example.flashmart.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<CartVO>> getCartItems(@RequestAttribute Long userId) {
        List<CartVO> cartList = cartService.getCartItems(userId);
        return Result.success(cartList);
    }

    @PostMapping
    public Result<String> addToCart(@Valid @RequestBody CartItemAddDTO cartItemAddDTO,
                                    @RequestAttribute Long userId) {
        cartService.addToCart(userId, cartItemAddDTO);
        return Result.success("加入购物车成功");
    }


    @PutMapping("/{cartId}")
    public Result<String> updateCart(@PathVariable @Positive(message = "购物车 ID 不合法") Integer cartId,
                                     @Valid @RequestBody CartItemUpdateDTO cartItemUpdateDTO,
                                     @RequestAttribute Long userId) {
        cartService.updateCart(userId, cartId, cartItemUpdateDTO);
        return Result.success("更新购物车成功");
    }

    @DeleteMapping("/{cartId}")
    public Result<String> removeCartItem(@PathVariable @Positive(message = "购物车 ID 不合法") Integer cartId,
                                         @RequestAttribute Long userId) {
        cartService.removeCartItem(userId, cartId);
        return Result.success("删除购物车商品成功");
    }

    @GetMapping("/count")
    public Result<Integer> countCartItems(@RequestAttribute Long userId) {
        return Result.success(cartService.countCartItems(userId));
    }

    @PutMapping("/selected/batch")
    public Result<String> updateSelectedBatch(@Valid @RequestBody CartSelectedBatchUpdateDTO dto,
                                              @RequestAttribute Long userId) {
        cartService.updateSelectedBatch(userId, dto);
        return Result.success("更新购物车勾选状态成功");
    }

    @DeleteMapping("/batch")
    public Result<String> removeCartItems(@Valid @RequestBody CartBatchDeleteDTO dto,
                                          @RequestAttribute Long userId) {
        cartService.removeCartItems(userId, dto);
        return Result.success("批量删除购物车商品成功");
    }
}
