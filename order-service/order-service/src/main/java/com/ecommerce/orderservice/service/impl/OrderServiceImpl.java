package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.Exceptions.*;
import com.ecommerce.orderservice.client.PaymentFeignClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderCompensationService;
import com.ecommerce.orderservice.service.OrderPersistenceService;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.client.ProductClient;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderPersistenceService orderPersistenceService;
    private final OrderCompensationService orderCompensationService;
    private final PaymentFeignClient paymentFeignClient;
    private final ProductClient productClient;


//
//    public OrderServiceImpl(OrderRepository orderRepository, OrderCompensationService orderCompensationService,ProductClient productClient,OrderPersistenceService orderPersistenceService) {
//        this.orderRepository = orderRepository;
//        this.orderCompensationService = orderCompensationService;
//        this.productClient = productClient;
//        this.orderPersistenceService = orderPersistenceService;
//    }

//    public OrderServiceImpl(OrderRepository orderRepository, OrderCompensationService orderCompensationService, RestTemplate restTemplate, ProductClient productClient) {
//        this.orderRepository = orderRepository;
//        this.restTemplate = restTemplate;
//        this.orderCompensationService = orderCompensationService;
//        this.productClient = productClient;
//    }
//


    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request, Long userId) {
        logger.info("UserId received from controller {}", userId);
        logger.info("PlaceOrder -> OrderServiceImpl");
        // 1. Validate product existence
        logger.info("Calling getProductById ");
        ProductResponse product = getProductById(request.getProductId());

        BigDecimal productPrice = product.getPrice();

        // 2. Create & save order
        logger.info("Creating new order");
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setPrice(productPrice);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setUserId(userId);
        logger.info("Saving order with status = {}", order.getStatus());
        Order savedOrder = orderPersistenceService.saveOrder(order);
        logger.info("order saved in savedOrder");


        // 3. Map to response
        return mapToResponse(savedOrder);

    }

    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    private ProductResponse getProductById(Long productId) {
        logger.info("getProductById method called");
//       try{
//            return restTemplate.getForObject("http://PRODUCT-SERVICE/api/products/{id}", ProductResponse.class,productId);
//        }catch (HttpClientErrorException.NotFound ex){
//            throw new ProductNotFoundException("Product not found with id " + productId);
//        } catch (RestClientException ex) {
//            throw new DownstreamServiceException("Product Service is currently unavailable");
//        }
        try {
            ProductResponse product = productClient.getProductById(productId);
            logger.info("getProductById try-block");
            return product;
        } catch (FeignException.NotFound ex) {
            logger.info("catch block executed -> product not available with id  {} ", productId);
            throw new ProductNotFoundException("Product not found with id " + productId);
        } catch (FeignException ex) {
            logger.info("product service down ");
            throw new DownstreamServiceException("Product Service is currently down or unavailable ");
        }
    }

    private ProductResponse productFallback(Long productId, Throwable ex) {
        logger.error("circuit breaker fallback triggered for product id {}", productId, ex);
        throw new DownstreamServiceException("Product Service is unavailable (circuit breaker active)");
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setProductId(order.getProductId());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setOrderId(order.getId());
        return response;

    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);


        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void payForOrder(Long orderId, Long userId) {

        logger.info("Processing payment for orderId={} by userId={}", orderId, userId);

        //1. Fetch Order
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found"));

        //2. Ensuring user owns this order
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized payment attempt");
        }

        //3. Ensuring order is pending payment
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOrderStateException("Order is not in Pending_Payment state");
        }
     /*
    //When i was dealing this payements internally then i was using this now i am using a sapreat microservice for this work
        //4. Simulate payment (80% success)
        boolean paymentSuccess = Math.random() < 0.8;


        if (paymentSuccess) {
            order.setStatus(OrderStatus.CONFIRMED);
            logger.info("Payment successful for orderId = {}", orderId);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            logger.warn("Payment failed for orderId={}", orderId);
        }
        orderRepository.save(order);

      */

       //4. call payment service via Feign
        PaymentResponseDto response;

        try{
            response = paymentFeignClient.createPayment(
                    userId,
                    new PaymentRequestDto(order.getId(),order.getPrice()));
        }catch (Exception ex){
            throw new PaymentServiceException("Payment service failed");
        }

        //5. Update order based on payment response
        if("SUCCESS".equals(response.getStatus())){
            logger.info("Payment successful for orderId={}",orderId);
            try {
                logger.info("in try block of product stock reduce in Order Service Impl");
                // reduce stock AFTER successful save
                reduceProductStock(order.getProductId(), order.getQuantity());
            } catch (RuntimeException ex) {
                logger.error("insufficient Product Stock catch block");
                orderCompensationService.cancelOrder(order.getId());
                throw new InsufficientStockException("Insufficient Stock");
            }
            order.setStatus(OrderStatus.CONFIRMED);
        }else{
            order.setStatus(OrderStatus.CANCELLED);
            logger.warn("Payment failed for orderId={}",orderId);
        }
        orderRepository.save(order);
    }

    private void reduceProductStock(Long productId, Integer quantity) {
//        restTemplate.put("http://PRODUCT-SERVICE/api/products/{id}/reduce-stock?quantity={qty}",null,productId,quantity);
        try {
            productClient.reduceStock(productId, quantity);
        } catch (FeignException.BadRequest ex) {
            throw new InsufficientStockException("Insufficient Stock");
        } catch (FeignException ex) {
            throw new DownstreamServiceException("Product Service is currently down or unavailable ");
        }
    }
}
