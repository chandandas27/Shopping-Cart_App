package com.chandan.shoppingcart.PaymentService.service;

import com.chandan.shoppingcart.PaymentService.model.PaymentRequest;
import com.chandan.shoppingcart.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
