package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.*;
import com.orderservice.sprint4.exception.InventoryException;
import com.orderservice.sprint4.exception.OrderNotFoundException;
import com.orderservice.sprint4.exception.OrderTransactionException;
import com.orderservice.sprint4.model.Order;
import com.orderservice.sprint4.model.OrderInvoice;
import com.orderservice.sprint4.model.OrderItem;
import com.orderservice.sprint4.model.ShipmentItem;
import com.orderservice.sprint4.model.enmus.OrderItemStatus;
import com.orderservice.sprint4.model.enmus.OrderStatus;
import com.orderservice.sprint4.model.enmus.ShipmentItemStatus;
import com.orderservice.sprint4.repository.OrderInvoiceRepository;
import com.orderservice.sprint4.repository.OrderItemRepository;
import com.orderservice.sprint4.repository.OrderRepository;
import com.orderservice.sprint4.repository.ShipmentItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService{
    @Value("${user.service.user.validation.url}")
    private String USER_SERVICE_USER_VALIDATION_URL;

    @Value("${inventory.update.url}")
    private String INVENTORY_UPDATE_URL;


    @Value("${product.service.product.validation.url}")
    private String PRODUCT_SERVICE_VALIDATION_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderInvoiceRepository orderInvoiceRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipmentItemRepository shipmentItemRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrderTransaction(OrderDetailsRequestDTO dto) {
        try {
            validateUser(dto.getUserId());
            for (OrderItemRequestDTO itemDto : dto.getOrderItemRequestDTOS()) {
                validateProduct(itemDto.getProductId());
            }

            Order order = new Order();
            order.setUserId(dto.getUserId());
            order.setOrderDate(dto.getOrderDate());
            order.setOrderStatus(OrderStatus.Pending);
            order.setPromoDiscount(dto.getPromoDiscount());
            order.setOrderTotal(dto.getOrderTotal());
            order.setAddressId(dto.getAddressId());

            Order savedOrder = orderRepository.save(order);

            List<OrderItem> orderItems = new ArrayList<>();
            Map<String, String> skuToOrderItemIdMap = new HashMap<>();

            // ✅ FIX: Declare inventoryPayload here
            List<Map<String, Object>> inventoryPayload = new ArrayList<>();

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
                item.setStatus(OrderItemStatus.Pending);
                item.setSellerId(itemDto.getSellerId());
                orderItems.add(item);
            }

            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

            for (OrderItem item : savedOrderItems) {
                skuToOrderItemIdMap.put(item.getSku(), "ORDITEM_" + item.getOrderItemId());

                Map<String, Object> inventoryData = new HashMap<>();
                inventoryData.put("sku", item.getSku());
                inventoryData.put("quantity", item.getQuantity());
                inventoryData.put("orderItemId", item.getOrderItemId());

                inventoryPayload.add(inventoryData);
            }

            OrderInvoice invoice = new OrderInvoice();
            invoice.setOrder(savedOrder);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setInvoiceAmount(savedOrder.getOrderTotal());
            invoice.setPaymentMode(dto.getPaymentMode());
            invoice.setInvoiceNumber(generateInvoiceNumber(dto.getUserId(), String.valueOf(dto.getPaymentMode())));
            orderInvoiceRepository.save(invoice);

            // Call inventory To update here
            try {
                updateInventoryStock(inventoryPayload);
                return OrderResponseDTO.builder()
                        .orderItemIds(skuToOrderItemIdMap)
                        .status("success")
                        .build();
            } catch (InventoryException ex) {
                return OrderResponseDTO.builder()
                        .orderItemIds(skuToOrderItemIdMap)
                        .status("failure")
                        .build();
            }

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
    public List<OrderSummaryDTO> getOrders(Integer months) {
        int userId = 101;

        try {
            // This should throw a custom UserNotFoundException (you can define it)
            validateUser(userId);

            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(months);

            List<Order> orders = orderRepository.findRecentOrdersByUserId(userId, cutoffDate);

            if (orders == null || orders.isEmpty()) {
                throw new OrderNotFoundException("No recent orders found for user ID: " + userId);
            }

            List<OrderSummaryDTO> response = null; // Added By Bipul Since No Response was Found
            for(Order order: orders){
                OrderSummaryDTO summaryDTO = new OrderSummaryDTO();
                summaryDTO.setOrderId(order.getOrderId());
                summaryDTO.setOrderDate(order.getOrderDate());
                summaryDTO.setOrderStatus(order.getOrderStatus());
                summaryDTO.setOrderTotal(order.getOrderTotal());
                summaryDTO.setItems(order.getOrderItems().stream().count());

                response.add(summaryDTO);
            }
            return response;

        } catch (OrderNotFoundException e) {
            throw e; // will be caught by your @ExceptionHandler
        } catch (Exception e) {
            throw new OrderTransactionException("Failed to fetch recent orders", e);
        }
    }

    private void validateUser(Integer userId) {

        try {
            String url = USER_SERVICE_USER_VALIDATION_URL + userId.toString();
            restTemplate.getForEntity(url, UserBasicInfoResponse.class).getBody(); // If 404, it will throw an exception
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RuntimeException("User with ID " + userId + " does not exist");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to verify user: " + ex.getMessage(), ex);
        }
    }

    private void validateProduct(Integer productId){
        try{
            String url = PRODUCT_SERVICE_VALIDATION_URL + productId;
            restTemplate.getForEntity(url,ProductDTO.class).getBody();
        }catch( HttpClientErrorException.NotFound ex){
            throw new RuntimeException("Product with ID " + productId + " does not exist");
        }catch (Exception ex){
            throw new RuntimeException("Failed to verify product: "+ex.getMessage(),ex);
        }
    }

    private String generateInvoiceNumber(Integer userId, String paymentMode) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "INV-" + timestamp + "-" + userId + "-" + paymentMode.toUpperCase();
    }

    private String generateTrackingId(Integer orderId,Integer orderItemId){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "TRK-"+timestamp+"-"+orderId+"-"+orderItemId;
    }

    private void updateInventoryStock(List<Map<String, Object>> updatePayload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(updatePayload, headers);

            restTemplate.exchange(INVENTORY_UPDATE_URL, HttpMethod.POST, requestEntity, Void.class);
        } catch (RestClientException ex) {
            throw new InventoryException("Failed to update inventory", ex);
        }
    }


    @Override
    public void orderConfirm(OrderStatusRequestDTO dto) {
        if (!dto.getOrderStatus().equals(OrderStatus.Cancelled) &&
                !dto.getOrderStatus().equals(OrderStatus.Ordered) &&
                !dto.getOrderStatus().equals(OrderStatus.Failed)) {

            throw new RuntimeException("Wrong input");
        }

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(dto.getOrderId()));

        List<OrderItem> items = order.getOrderItems();

        if (!order.getOrderStatus().equals(OrderStatus.Pending)) {
            throw new RuntimeException("Order already uptodate");
        }
        if(dto.getOrderStatus().equals(OrderStatus.Ordered)) {

            items.stream().forEach(item -> {
                item.setStatus(OrderItemStatus.Ordered);
                orderItemRepository.save(item);
                ShipmentItem shipmentItem = new ShipmentItem();
                shipmentItem.setOrderItem(item);
                shipmentItem.setItemTrackingId(generateTrackingId(item.getOrder().getOrderId(), item.getOrderItemId()));
                shipmentItem.setItemStatus(ShipmentItemStatus.Pending);
                shipmentItem.setShipmentDate(LocalDateTime.now());
                shipmentItem.setDeliveredDate(LocalDateTime.now().plusDays(7));


                shipmentItemRepository.save(shipmentItem);
            });

            order.setOrderStatus(dto.getOrderStatus());

            orderRepository.saveAndFlush(order);
            return;
        }if(dto.getOrderStatus().equals(OrderStatus.Cancelled)){
            items.stream().forEach(item->{
                item.setStatus(OrderItemStatus.Cancelled);
                orderItemRepository.save(item);
            });
            order.setOrderStatus(dto.getOrderStatus());

            orderRepository.saveAndFlush(order);
            return;
        }if(dto.getOrderStatus().equals(OrderStatus.Failed)){
            items.stream().forEach(item->{
                item.setStatus(OrderItemStatus.Failed);
                orderItemRepository.save(item);
            });
            order.setOrderStatus(dto.getOrderStatus());

            orderRepository.saveAndFlush(order);
        }
    }
}
