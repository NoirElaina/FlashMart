package org.example.flashmart.checkout.controller;

import jakarta.validation.Valid;
import org.example.flashmart.checkout.model.dto.CheckoutPreviewDTO;
import org.example.flashmart.checkout.model.vo.CheckoutPreviewVO;
import org.example.flashmart.checkout.service.CheckoutService;
import org.example.flashmart.common.response.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/preview")
    public Result<CheckoutPreviewVO> preview(@RequestAttribute Long userId, @Valid @RequestBody CheckoutPreviewDTO dto) {
        return Result.success(checkoutService.preview(userId, dto));
    }
}
