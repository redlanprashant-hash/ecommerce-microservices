package com.ecommerce.productservice.ProductServiceImpl;

import com.ecommerce.productservice.ProductService.ProductService;
import com.ecommerce.productservice.exceptions.DuplicateSkuException;
import com.ecommerce.productservice.exceptions.InsufficientStockException;
import com.ecommerce.productservice.exceptions.ProductNotFoundException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @Override
    public Product createProduct(Product product) {
        logger.info("createProduct from ProductServiceImpl");
        productRepository.findBySku(product.getSku())
                .ifPresent(existing -> {
                    throw new DuplicateSkuException("Product with this SKU already exists");
                });

        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public void reduceStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if(product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock");
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

    }
}
