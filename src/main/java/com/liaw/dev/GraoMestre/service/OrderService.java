package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.config.SecurityUtils;
import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.Payment;
import com.liaw.dev.GraoMestre.entity.Product;
import com.liaw.dev.GraoMestre.entity.User;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import com.liaw.dev.GraoMestre.exception.exceptions.ConflitException;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.OrderMapper;
import com.liaw.dev.GraoMestre.repository.OrderRepository;
import com.liaw.dev.GraoMestre.repository.ProductRepository;
import com.liaw.dev.GraoMestre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + userId));

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        List<Long> productIds = orderRequestDTO.getItems().stream()
                .map(itemDto -> itemDto.getProductId())
                .collect(Collectors.toList());

        Map<Long, Product> productsMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (var itemDto : orderRequestDTO.getItems()) {
            Product product = productsMap.get(itemDto.getProductId());

            if (product == null || !product.getActive()) {
                throw new EntityNotFoundException("Produto não encontrado ou inativo com ID: " + itemDto.getProductId());
            }
            if (product.getStorage() < itemDto.getQuantity()) {
                throw new ConflitException("Estoque insuficiente para o produto: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtTime(product.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
            totalOrderPrice = totalOrderPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            product.setStorage(product.getStorage() - itemDto.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalOrderPrice);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTotalPrice(totalOrderPrice);
        order.setPayment(payment);

        order = orderRepository.save(order);
        return OrderMapper.toOrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrderHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderRepository.findByUser_Id(userId);
        return OrderMapper.toOrderResponseDTOList(orders);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getMyOrderDetails(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ConflitException("Você não tem permissão para visualizar este pedido.");
        }
        return OrderMapper.toOrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrdersByStatus(OrderStatus status) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderRepository.findByUser_IdAndOrderStatus(userId, status);
        return OrderMapper.toOrderResponseDTOList(orders);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(OrderMapper::toOrderResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> filterOrders(
            OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long userId, Pageable pageable) {

        Page<Order> orders;

        if (status != null && startDate != null && endDate != null && userId != null) {
            orders = orderRepository.findByOrderStatusAndOrderDateBetweenAndUser_Id(status, startDate, endDate, userId, pageable);
        } else if (status != null && startDate != null && endDate != null) {
            orders = orderRepository.findByOrderStatusAndOrderDateBetween(status, startDate, endDate, pageable);
        } else if (status != null && userId != null) {
            orders = orderRepository.findByOrderStatusAndUser_Id(status, userId, pageable);
        } else if (startDate != null && endDate != null && userId != null) {
            orders = orderRepository.findByOrderDateBetweenAndUser_Id(startDate, endDate, userId, pageable);
        } else if (startDate != null && endDate != null) {
            orders = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        } else if (userId != null) {
            orders = orderRepository.findByUser_Id(userId, pageable);
        } else if (status != null) {
            orders = orderRepository.findByOrderStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(OrderMapper::toOrderResponseDTO);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetailsForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));
        return OrderMapper.toOrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));

        if (!isValidStatusTransition(order.getOrderStatus(), newStatus)) {
            throw new ConflitException("Transição de status inválida de " + order.getOrderStatus() + " para " + newStatus);
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.COMPLETED && order.getPayment() != null) {
            order.getPayment().setPaymentStatus(PaymentStatus.PAID);
        }

        if (newStatus == OrderStatus.CANCELED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStorage(product.getStorage() + item.getQuantity());
                productRepository.save(product);
            }
            if (order.getPayment() != null) {
                order.getPayment().setPaymentStatus(PaymentStatus.CANCELED);
            }
        }

        order = orderRepository.save(order);
        return OrderMapper.toOrderResponseDTO(order);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELED;
            case PAID:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELED;
            case PROCESSING:
                return newStatus == OrderStatus.SENDED || newStatus == OrderStatus.CANCELED;
            case SENDED:
                return newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.RECUSE;
            case COMPLETED:
            case CANCELED:
            case RECUSE:
                return false;
            default:
                return false;
        }
    }
}