package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.config.SecurityUtils;
import com.liaw.dev.GraoMestre.dto.request.OrderItemRequestDTO;
import com.liaw.dev.GraoMestre.dto.request.OrderRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.OrderResponseDTO;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.Payment;
import com.liaw.dev.GraoMestre.entity.Product;
import com.liaw.dev.GraoMestre.entity.User;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.PaymentStatus;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.exception.exceptions.ConflitException;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.OrderMapper;
import com.liaw.dev.GraoMestre.repository.OrderItemRepository;
import com.liaw.dev.GraoMestre.repository.OrderRepository;
import com.liaw.dev.GraoMestre.repository.ProductRepository;
import com.liaw.dev.GraoMestre.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + userId));

        // Verificar se já existe um pedido PENDING para este usuário
        Optional<Order> existingPendingOrder = orderRepository.findByUser_IdAndOrderStatus(userId, OrderStatus.PENDING)
                .stream().findFirst(); // Pega o primeiro, se houver

        if (existingPendingOrder.isPresent()) {
            // Se já existe um pedido PENDING, adicione os itens a ele
            Order order = existingPendingOrder.get();
            return addItemsToExistingOrder(order, orderRequestDTO.getItems());
        } else {
            // Se não existe, cria um novo pedido
            Order order = new Order();
            order.setUser(user);
            order.setPaymentMethod(orderRequestDTO.getPaymentMethod());
            order.setOrderStatus(OrderStatus.PENDING); // Garante que o status inicial é PENDING

            // Processa os itens para o novo pedido
            List<OrderItem> orderItems = processOrderItems(order, orderRequestDTO.getItems());
            order.setOrderItems(orderItems);

            BigDecimal totalOrderPrice = calculateTotalPrice(orderItems);
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
    }

    @Transactional
    public OrderResponseDTO addItemToOrder(Long orderId, OrderItemRequestDTO itemRequestDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ConflitException("Você não tem permissão para modificar este pedido.");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ConflitException("Não é possível adicionar itens a um pedido que não está PENDING.");
        }

        return addItemsToExistingOrder(order, List.of(itemRequestDTO));
    }

    private OrderResponseDTO addItemsToExistingOrder(Order order, List<OrderItemRequestDTO> newItems) {
        BigDecimal currentOrderPrice = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;

        List<Long> productIds = newItems.stream()
                .map(OrderItemRequestDTO::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productsMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (OrderItemRequestDTO newItemDto : newItems) {
            Product product = productsMap.get(newItemDto.getProductId());

            if (product == null || !product.getActive()) {
                throw new EntityNotFoundException("Produto não encontrado ou inativo com ID: " + newItemDto.getProductId());
            }

            Optional<OrderItem> existingOrderItemOpt = order.getOrderItems().stream()
                    .filter(oi -> oi.getProduct().getId().equals(newItemDto.getProductId()))
                    .findFirst();

            if (existingOrderItemOpt.isPresent()) {
                OrderItem existingItem = existingOrderItemOpt.get();
                int oldQuantity = existingItem.getQuantity();
                int newTotalQuantity = oldQuantity + newItemDto.getQuantity();

                if (product.getStorage() < newItemDto.getQuantity()) { // Verifica apenas a quantidade *adicional*
                    throw new ConflitException("Estoque insuficiente para adicionar mais do produto: " + product.getName());
                }

                existingItem.setQuantity(newTotalQuantity);
                currentOrderPrice = currentOrderPrice.add(product.getPrice().multiply(BigDecimal.valueOf(newItemDto.getQuantity())));
                product.setStorage(product.getStorage() - newItemDto.getQuantity()); // Reduz estoque pela quantidade adicionada
            } else {
                if (product.getStorage() < newItemDto.getQuantity()) {
                    throw new ConflitException("Estoque insuficiente para o produto: " + product.getName());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(newItemDto.getQuantity());
                orderItem.setPriceAtTime(product.getPrice());
                orderItem.setOrder(order);
                order.getOrderItems().add(orderItem); // Adiciona à lista do pedido

                currentOrderPrice = currentOrderPrice.add(product.getPrice().multiply(BigDecimal.valueOf(newItemDto.getQuantity())));
                product.setStorage(product.getStorage() - newItemDto.getQuantity()); // Reduz estoque
            }
            productRepository.save(product); // Salva o produto com estoque atualizado
        }

        order.setTotalPrice(currentOrderPrice);
        if (order.getPayment() != null) {
            order.getPayment().setTotalPrice(currentOrderPrice);
        }
        order = orderRepository.save(order); // Salva o pedido com os itens atualizados
        return OrderMapper.toOrderResponseDTO(order);
    }

    private List<OrderItem> processOrderItems(Order order, List<OrderItemRequestDTO> itemDtos) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<Long> productIds = itemDtos.stream()
                .map(OrderItemRequestDTO::getProductId)
                .collect(Collectors.toList());

        Map<Long, Product> productsMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (var itemDto : itemDtos) {
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

            product.setStorage(product.getStorage() - itemDto.getQuantity());
            productRepository.save(product);
        }
        return orderItems;
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public OrderResponseDTO removeItemFromOrder(Long orderId, Long orderItemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ConflitException("Você não tem permissão para modificar este pedido.");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ConflitException("Não é possível remover itens de um pedido que não está PENDING.");
        }

        OrderItem itemToRemove = order.getOrderItems().stream()
                .filter(oi -> oi.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item do pedido não encontrado com ID: " + orderItemId));

        // Devolve o estoque
        Product product = itemToRemove.getProduct();
        product.setStorage(product.getStorage() + itemToRemove.getQuantity());
        productRepository.save(product);

        // Remove o item da lista e atualiza o total
        order.getOrderItems().remove(itemToRemove);
        orderItemRepository.delete(itemToRemove); // Exclui o OrderItem do banco de dados

        BigDecimal newTotalPrice = calculateTotalPrice(order.getOrderItems());
        order.setTotalPrice(newTotalPrice);
        if (order.getPayment() != null) {
            order.getPayment().setTotalPrice(newTotalPrice);
        }

        order = orderRepository.save(order);
        return OrderMapper.toOrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateOrderItemQuantity(Long orderId, Long orderItemId, Integer newQuantity) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ConflitException("Você não tem permissão para modificar este pedido.");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ConflitException("Não é possível atualizar a quantidade de itens em um pedido que não está PENDING.");
        }

        OrderItem itemToUpdate = order.getOrderItems().stream()
                .filter(oi -> oi.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item do pedido não encontrado com ID: " + orderItemId));

        Product product = itemToUpdate.getProduct();
        int oldQuantity = itemToUpdate.getQuantity();
        int quantityDifference = newQuantity - oldQuantity;

        if (quantityDifference > 0) { // Aumentando a quantidade
            if (product.getStorage() < quantityDifference) {
                throw new ConflitException("Estoque insuficiente para aumentar a quantidade do produto: " + product.getName());
            }
            product.setStorage(product.getStorage() - quantityDifference);
        } else if (quantityDifference < 0) { // Diminuindo a quantidade
            product.setStorage(product.getStorage() - quantityDifference); // Adiciona de volta ao estoque
        }
        productRepository.save(product);

        itemToUpdate.setQuantity(newQuantity);
        BigDecimal newTotalPrice = calculateTotalPrice(order.getOrderItems()); // Recalcula com o item atualizado
        order.setTotalPrice(newTotalPrice);
        if (order.getPayment() != null) {
            order.getPayment().setTotalPrice(newTotalPrice);
        }

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
                return newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELED || newStatus == OrderStatus.PROCESSING;
            case PAID:
                return newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELED || newStatus == OrderStatus.COMPLETED;
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

    private LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    public List<OrderResponseDTO> findOrdersForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = getStartOfDay(today);
        LocalDateTime end = getEndOfDay(today);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = getStartOfDay(yesterday);
        LocalDateTime end = getEndOfDay(yesterday);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime start = getStartOfDay(startOfWeek);
        LocalDateTime end = getEndOfDay(endOfWeek);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate endOfLastWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).minusWeeks(1);
        LocalDate startOfLastWeek = endOfLastWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDateTime start = getStartOfDay(startOfLastWeek);
        LocalDateTime end = getEndOfDay(endOfLastWeek);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime start = getStartOfDay(startOfMonth);
        LocalDateTime end = getEndOfDay(endOfMonth);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = startOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime start = getStartOfDay(startOfLastMonth);
        LocalDateTime end = getEndOfDay(endOfLastMonth);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersForCustomPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Para período CUSTOM, startDate e endDate são obrigatórios");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }

        LocalDateTime start = getStartOfDay(startDate);
        LocalDateTime end = getEndOfDay(endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        return orders.stream().map(OrderMapper::toOrderResponseDTO).toList();
    }

    public List<OrderResponseDTO> findOrdersByTimePeriod(TimePeriod timePeriod, LocalDate startDate, LocalDate endDate) {
        switch (timePeriod) {
            case TODAY:
                return findOrdersForToday();
            case YESTERDAY:
                return findOrdersForYesterday();
            case THIS_WEEK:
                return findOrdersForThisWeek();
            case LAST_WEEK:
                return findOrdersForLastWeek();
            case THIS_MONTH:
                return findOrdersForThisMonth();
            case LAST_MONTH:
                return findOrdersForLastMonth();
            case CUSTOM:
                return findOrdersForCustomPeriod(startDate, endDate);
            default:
                throw new IllegalArgumentException("Período de tempo inválido");
        }
    }
}