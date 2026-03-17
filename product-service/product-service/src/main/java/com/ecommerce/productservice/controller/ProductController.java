package com.ecommerce.productservice.controller;




import com.ecommerce.productservice.ProductService.ProductService;
import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.model.Product;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request){
        logger.info("createProduct controller ");
        Product product = new Product(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getSku()
        );

        Product savedProduct = productService.createProduct(product);

        return new ResponseEntity<>(mapToResponse(savedProduct), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) throws InterruptedException {
//        Thread.sleep(5000);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}/{reduce-stock}")
    public ResponseEntity<Void> reduceStock(@PathVariable Long productId, @RequestParam Integer quantity){
        productService.reduceStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }


    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSku(product.getSku());
        return response;
    }
}
