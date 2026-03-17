package com.ecommerce.productservice.ProductService;


import com.ecommerce.productservice.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);

    Product getProductById(Long id);

    List<Product> getAllProducts();

    void reduceStock(Long productId, Integer quantity);
}
