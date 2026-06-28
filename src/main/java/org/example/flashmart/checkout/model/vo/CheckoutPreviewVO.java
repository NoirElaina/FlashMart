package org.example.flashmart.checkout.model.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutPreviewVO {
    private List<CheckoutItemVO> items;
    private Integer totalQuantity;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private Boolean canSubmit;
}
