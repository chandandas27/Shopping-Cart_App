package com.chandan.shoppingcart.ProductService.service;

import com.chandan.shoppingcart.ProductService.model.ProductRequest;
import com.chandan.shoppingcart.ProductService.model.ProductResponse;

public interface ProductService {

    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);

}
