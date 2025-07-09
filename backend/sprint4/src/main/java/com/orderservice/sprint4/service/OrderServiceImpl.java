package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.model.Order;
import com.orderservice.sprint4.model.OrderInvoice;
import com.orderservice.sprint4.model.OrderItem;
import com.orderservice.sprint4.model.enmus.OrderItemStatus;
import com.orderservice.sprint4.model.enmus.OrderStatus;
import com.orderservice.sprint4.repository.OrderInvoiceRepository;
import com.orderservice.sprint4.repository.OrderItemRepository;
import com.orderservice.sprint4.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{
//    @Value("${user.service.user.validation.url}")
//    private String USER_SERVICE_USER_VALIDATION_URL;
//
//    @Value("${product.service.product.validation.url}")
//    private String PRODUCT_SERVICE_VALIDATION_URL;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private OrderInvoiceRepository orderInvoiceRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Override
    @Transactional
    public String createOrderTransaction(OrderDetailsRequestDTO dto) {
        try {

            validateUser(dto.getUserId());

            for (OrderItemRequestDTO itemDto : dto.getOrderItemRequestDTOS()) {
                validateProduct(itemDto.getProductId());
            }



            Order order = new Order();
            order.setUserId(dto.getUserId());
            order.setOrderDate(dto.getOrderDate());
//            order.setOrderStatus(dto.getOrderStatus());
            order.setOrderStatus(OrderStatus.Pending);
            order.setPromoDiscount(dto.getPromoDiscount());
            order.setOrderTotal(dto.getOrderTotal());
            order.setAddressId(dto.getAddressId());

            Order savedOrder = orderRepository.save(order);
            System.out.println(savedOrder.toString());


            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequestDTO itemDto : dto.getOrderItemRequestDTOS()) {
                OrderItem item = new OrderItem();
                item.setOrder(savedOrder);
                item.setProductId(itemDto.getProductId());
                item.setSku(itemDto.getSku());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setDiscount(itemDto.getDiscount());
                item.setFinalPrice(itemDto.getFinalPrice());
                item.setSize(itemDto.getSize());
//                item.setStatus(itemDto.getStatus());
                item.setStatus(OrderItemStatus.Pending);
                item.setSellerId(itemDto.getSellerId());
                orderItems.add(item);
            }
            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

            savedOrderItems.stream().toString();


            OrderInvoice invoice = new OrderInvoice();
            invoice.setOrder(savedOrder);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setInvoiceAmount(savedOrder.getOrderTotal());
            invoice.setPaymentMode(dto.getPaymentMode());


            String invoiceNumber = generateInvoiceNumber(dto.getUserId(), String.valueOf(dto.getPaymentMode()));
            invoice.setInvoiceNumber(invoiceNumber);

            orderInvoiceRepository.save(invoice);

            return invoiceNumber;

        } catch (Exception e) {
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }


    @Override
    public OrderDetailsResponseDTO getOrderDetails(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDetailsResponseDTO response = new OrderDetailsResponseDTO();


        Arrays.stream(response.getClass().getMethods())
                .map(Method::getName)
                .distinct()
                .forEach(System.out::println);

        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setOrderStatus(order.getOrderStatus());
        response.setPromoDiscount(order.getPromoDiscount());
        response.setOrderTotal(order.getOrderTotal());


        if (order.getOrderInvoice() != null) {
            response.setInvoiceNumber(order.getOrderInvoice().getInvoiceNumber());
            response.setInvoiceDate(order.getOrderInvoice().getInvoiceDate());
            response.setInvoiceAmount(order.getOrderInvoice().getInvoiceAmount());
            response.setPaymentMode(order.getOrderInvoice().getPaymentMode());
        }

        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream().map(item -> {
            OrderItemResponseDTO dto = new OrderItemResponseDTO();
            dto.setOrderItemId(item.getOrderItemId());
            dto.setProductId(item.getProductId());
            dto.setSku(item.getSku());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setDiscount(item.getDiscount());
            dto.setFinalPrice(item.getFinalPrice());
            dto.setSize(item.getSize());
            dto.setStatus(item.getStatus());
            dto.setSellerId(item.getSellerId());
            return dto;
        }).toList();

        response.setOrderItems(itemDTOs);
        return response;
    }

    @Override
    public List<OrderSummaryDTO> getOrders(Integer months){
        int userId = 101;

        validateUser(userId);

        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(months);

        List<Order> orders = orderRepository.findRecentOrdersByUserId(userId,cutoffDate);

        List<OrderSummaryDTO> response = new ArrayList<>();

        for(Order order: orders){
            OrderSummaryDTO summaryDTO = OrderSummaryDTO.builder()
                    .orderId(order.getOrderId())
                    .orderDate(order.getOrderDate())
                    .orderStatus(order.getOrderStatus())
                    .orderTotal(order.getOrderTotal())
                    .items(order.getOrderItems().stream().count()).build();
            response.add(summaryDTO);

        }
        return response;

    }


    private void validateUser(Integer userId) {
        return;
//        try {
//            String url = USER_SERVICE_USER_VALIDATION_URL + userId;
//            restTemplate.getForEntity(url, String.class); // If 404, it will throw an exception
//        } catch (HttpClientErrorException.NotFound ex) {
//            throw new RuntimeException("User with ID " + userId + " does not exist");
//        } catch (Exception ex) {
//            throw new RuntimeException("Failed to verify user: " + ex.getMessage(), ex);
//        }
    }

    private void validateProduct(Integer productId){
        return;
//        try{
//            String url = PRODUCT_SERVICE_VALIDATION_URL + productId;
//            restTemplate.getForEntity(url,String.class);
//        }catch( HttpClientErrorException.NotFound ex){
//            throw new RuntimeException("Product with ID " + productId + " does not exist");
//        }catch (Exception ex){
//            throw new RuntimeException("Failed to verify product: "+ex.getMessage(),ex);
//        }
    }


    private String generateInvoiceNumber(Integer userId, String paymentMode) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "INV-" + timestamp + "-" + userId + "-" + paymentMode.toUpperCase();
    }
}
