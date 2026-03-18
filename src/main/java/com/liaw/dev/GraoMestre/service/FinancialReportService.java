package com.liaw.dev.GraoMestre.service; // Ajustado para o pacote do seu projeto atual

import com.liaw.dev.GraoMestre.dto.response.ExpenseResponseDTO;
import com.liaw.dev.GraoMestre.dto.response.TopProductResponse;
import com.liaw.dev.GraoMestre.enums.OrderStatus;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.entity.Order;
import com.liaw.dev.GraoMestre.entity.OrderItem;
import com.liaw.dev.GraoMestre.entity.Product;
import com.liaw.dev.GraoMestre.entity.Category;
import com.liaw.dev.GraoMestre.repository.OrderRepository; // Ajustar o pacote do repositório
// Ajustar o pacote do DTO se necessário
import com.liaw.dev.GraoMestre.service.ExpenseService; // Assumindo que ExpenseService está no mesmo pacote ou importado

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialReportService {

    private final OrderRepository orderRepository;
    private final ExpenseService expenseService;

    @Autowired
    public FinancialReportService(OrderRepository orderRepository, ExpenseService expenseService) {
        this.orderRepository = orderRepository;
        this.expenseService = expenseService;
    }

    @Getter
    @Setter
    public static class FinancialReport {
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private BigDecimal totalExpenses = BigDecimal.ZERO;
        private int totalOrders = 0;
        private int completedOrders = 0;
        private int canceledOrders = 0;
        private int pendingOrders = 0; // Inclui PENDING e RECUSE
        private int processingOrders = 0; // Pedidos em processamento

        private BigDecimal netProfit = BigDecimal.ZERO;
        private Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        private Map<String, BigDecimal> revenueByProduct = new HashMap<>();
        private Map<String, Integer> quantitySoldByProduct = new HashMap<>();
        private Map<String, Integer> quantitySoldByCategory = new HashMap<>();
        private Map<String, BigDecimal> revenueByPaymentMethod = new HashMap<>(); // Nova métrica

        public BigDecimal getNetProfit() {
            return totalRevenue.subtract(totalExpenses);
        }
    }

    private FinancialReport calculateFinancialReport(List<Order> orders) {
        FinancialReport report = new FinancialReport();

        for (Order order : orders) {
            report.setTotalOrders(report.getTotalOrders() + 1);

            if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                report.setCompletedOrders(report.getCompletedOrders() + 1);

                if (order.getTotalPrice() != null) {
                    report.setTotalRevenue(report.getTotalRevenue().add(order.getTotalPrice()));
                    if (order.getPaymentMethod() != null) {
                        String paymentMethodName = order.getPaymentMethod().name();
                        report.getRevenueByPaymentMethod().merge(paymentMethodName, order.getTotalPrice(), BigDecimal::add);
                    }
                }

                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProduct() != null && item.getPriceAtTime() != null && item.getQuantity() != null) {
                            String productName = item.getProduct().getName();
                            BigDecimal itemTotal = item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity()));

                            report.getRevenueByProduct().merge(productName, itemTotal, BigDecimal::add);
                            report.getQuantitySoldByProduct().merge(productName, item.getQuantity(), Integer::sum);

                            if (item.getProduct().getCategory() != null) {
                                String categoryName = item.getProduct().getCategory().getName();
                                report.getRevenueByCategory().merge(categoryName, itemTotal, BigDecimal::add);
                                report.getQuantitySoldByCategory().merge(categoryName, item.getQuantity(), Integer::sum);
                            }
                        }
                    }
                }

            } else if (order.getOrderStatus() == OrderStatus.CANCELED) {
                report.setCanceledOrders(report.getCanceledOrders() + 1);
            } else if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.RECUSE) {
                report.setPendingOrders(report.getPendingOrders() + 1);
            } else if (order.getOrderStatus() == OrderStatus.PROCESSING) {
                report.setProcessingOrders(report.getProcessingOrders() + 1);
                report.setPendingOrders(report.getPendingOrders() + 1);
            }
        }
        return report;
    }

    public FinancialReport generateFinancialReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        FinancialReport report = calculateFinancialReport(orders);

        LocalDate startLocalDate = startDate.toLocalDate();
        LocalDate endLocalDate = endDate.toLocalDate();

        List<ExpenseResponseDTO> expensesList = expenseService.findExpensesForCustomPeriod(startLocalDate, endLocalDate);
        BigDecimal totalExpenses = expensesList.stream()
                .map(ExpenseResponseDTO::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalExpenses(totalExpenses);
        report.setNetProfit(report.getTotalRevenue().subtract(totalExpenses));

        return report;
    }

    public FinancialReport generateFinancialReport(TimePeriod timePeriod) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        LocalDateTime now = LocalDateTime.now();

        switch (timePeriod) {
            case TODAY:
                startDate = LocalDate.now().atStartOfDay();
                endDate = LocalDate.now().atTime(23, 59, 59);
                break;
            case YESTERDAY:
                startDate = LocalDate.now().minusDays(1).atStartOfDay();
                endDate = LocalDate.now().minusDays(1).atTime(23, 59, 59);
                break;
            case THIS_WEEK:
                startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                endDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);
                break;
            case LAST_WEEK:
                startDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                endDate = LocalDate.now().minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(23, 59, 59);
                break;
            case THIS_MONTH:
                startDate = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                break;
            case LAST_MONTH:
                startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
                endDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                break;
            case CUSTOM:
            default:
                throw new IllegalArgumentException("Período inválido: " + timePeriod);
        }

        return generateFinancialReport(startDate, endDate);
    }

    public FinancialReport getDailyFinancialSummary() {
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(23, 59, 59);
        return generateFinancialReport(startDate, endDate);
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(23, 59, 59);
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED && order.getTotalPrice() != null)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Integer> getTodayPendingAndProcessingOrdersCount() {
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atTime(23, 59, 59);
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        int pending = (int) orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.RECUSE)
                .count();
        int processing = (int) orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PROCESSING)
                .count();

        Map<String, Integer> counts = new HashMap<>();
        counts.put("pending", pending);
        counts.put("processing", processing);
        return counts;
    }

    public BigDecimal getProductRevenueByPeriod(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId))
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCategoryRevenueByPeriod(Long categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getCategory() != null && item.getProduct().getCategory().getId().equals(categoryId))
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getProductQuantitySoldByPeriod(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId))
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public Integer getCategoryQuantitySoldByPeriod(Long categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getCategory() != null && item.getProduct().getCategory().getId().equals(categoryId))
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public List<TopProductResponse> getTopNProductsByRevenue(
            int limit,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        Map<String, BigDecimal> productRevenue = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null &&
                        item.getPriceAtTime() != null &&
                        item.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> item.getPriceAtTime()
                                        .multiply(BigDecimal.valueOf(item.getQuantity())),
                                BigDecimal::add
                        )
                ));

        return productRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new TopProductResponse(entry.getKey(), entry.getValue())) // 🔥 AQUI ESTÁ A CORREÇÃO
                .toList();
    }

    public List<TopProductResponse> getTopNProductsByQuantitySold(
            int limit,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);

        Map<String, Integer> productQuantity = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .flatMap(order -> order.getOrderItems().stream())
                .filter(item -> item.getProduct() != null && item.getQuantity() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        return productQuantity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new TopProductResponse(
                        entry.getKey(),
                        BigDecimal.valueOf(entry.getValue())
                ))
                .toList();
    }

    public BigDecimal getAverageOrderValue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        long completedOrdersCount = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED && order.getTotalPrice() != null)
                .count();

        if (completedOrdersCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRevenue = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED && order.getTotalPrice() != null)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRevenue.divide(BigDecimal.valueOf(completedOrdersCount), 2, BigDecimal.ROUND_HALF_UP);
    }
}