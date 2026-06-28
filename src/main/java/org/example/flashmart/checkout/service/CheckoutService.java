package org.example.flashmart.checkout.service;

import org.example.flashmart.checkout.model.dto.CheckoutPreviewDTO;
import org.example.flashmart.checkout.model.vo.CheckoutPreviewVO;

public interface CheckoutService {
    CheckoutPreviewVO preview(Long userId, CheckoutPreviewDTO dto);
}
