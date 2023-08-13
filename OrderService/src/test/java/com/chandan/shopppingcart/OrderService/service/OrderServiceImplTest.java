package com.chandan.shopppingcart.OrderService.service;

import com.chandan.shoppingcart.ProductService.model.ProductResponse;
import com.chandan.shopppingcart.OrderService.entity.Order;
import com.chandan.shopppingcart.OrderService.exception.CustomException;
import com.chandan.shopppingcart.OrderService.external.client.PaymentService;
import com.chandan.shopppingcart.OrderService.external.client.ProductService;
import com.chandan.shopppingcart.OrderService.external.request.PaymentRequest;
import com.chandan.shopppingcart.OrderService.external.response.PaymentResponse;
import com.chandan.shopppingcart.OrderService.model.OrderRequest;
import com.chandan.shopppingcart.OrderService.model.OrderResponse;
import com.chandan.shopppingcart.OrderService.model.PaymentMode;
import com.chandan.shopppingcart.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {


    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;


    @InjectMocks
    OrderService orderService = new OrderServiceImpl();


    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){

        //Mocking

        Order order = getMockOrder();

        when((orderRepository.findById(anyLong())))
                .thenReturn(Optional.of(order));

        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+ order.getProductId(),
                ProductResponse.class))
                .thenReturn(getMockProductResponse());

        when(restTemplate.getForObject( "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class))
                .thenReturn(getMockPaymentResponse());

        //Actual

        OrderResponse orderResponse = orderService.getOrderDetails(1);

        //Verification

        verify(orderRepository,times(1)).findById(anyLong());
        verify(restTemplate,times(1)).getForObject("http://PRODUCT-SERVICE/product/"+ order.getProductId(),
                ProductResponse.class);
        verify(restTemplate,times(1)).getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class);


        //Assert

        assertNotNull(orderResponse);
        assertEquals(order.getId(), orderResponse.getOrderId());

    }

    @DisplayName("Get Orders - Failure Scenario")
    @Test
    void test_When_Get_Order_Not_Found_then_Not_Found(){


        when((orderRepository.findById(anyLong())))
                .thenReturn(Optional.ofNullable(null));



        CustomException exception = assertThrows(CustomException.class,() -> orderService.getOrderDetails(1));

        assertEquals("NOT_FOUND", exception.getErrorCode());
        assertEquals(404, exception.getStatus());

        verify(orderRepository, times(1)).findById(anyLong());

    }

    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success() {
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L,HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2))
                .save(any());
        verify(productService, times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService, times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(), orderId);
    }


    @DisplayName("Place Order - Payment Failed Scenario")
    @Test
    void test_when_Place_Order_Payment_Fails_then_Order_Placed() {

        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2))
                .save(any());
        verify(productService, times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService, times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(), orderId);
    }


    private OrderRequest getMockOrderRequest() {

        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(100)
                .build();
    }
    private PaymentResponse getMockPaymentResponse() {

        return PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200)
                .orderId(1)
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {

        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {

        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(100)
                .quantity(200)
                .productId(2)
                .build();
    }


}