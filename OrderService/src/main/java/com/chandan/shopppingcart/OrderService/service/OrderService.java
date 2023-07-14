package com.chandan.shopppingcart.OrderService.service;

import com.chandan.shopppingcart.OrderService.model.OrderRequest;
import com.chandan.shopppingcart.OrderService.model.OrderResponse;

public interface OrderService {


    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
