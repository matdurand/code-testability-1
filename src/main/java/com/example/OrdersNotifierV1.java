package com.example;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

public class OrdersNotifierV1 {
    private final UserRepository userRepository;

    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final SalesReportRepository salesReportRepository;

    private static final String MANAGER_EMAIL_SUBJECT = "Sales are now above daily objectives";
    private static final String MANAGER_MESSAGE = "Sales for today have reached the expected threshold.";

    public OrdersNotifierV1(
            UserRepository userRepository,
            OrderRepository orderRepository,
            SalesReportRepository salesReportRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.salesReportRepository = salesReportRepository;
        this.emailService = emailService;
    }

    public void onNewOrder(Order order) {
        sendNewOrderEmailTo(order, order.getMadeBy().getEmail());
        if (isLocalManagerThresholdReachedForToday(order)) {
            sendDailySalesOKToLocalManagers();
        }
        if (isRegionalManagerThresholdReachedForToday(order)) {
            sendDailySalesOKToRegionalManagers();
        }
    }

    private void sendDailySalesOKToLocalManagers() {
        String emailSubject = MANAGER_EMAIL_SUBJECT;
        String emailBody = "Hi local managers. " + MANAGER_MESSAGE;
        List<String> emails = getEmailsForRole(Role.LOCAL_MANAGER);
        this.emailService.sendMail(emailSubject, emailBody, emails);
    }

    private void sendDailySalesOKToRegionalManagers() {
        String emailSubject = MANAGER_EMAIL_SUBJECT;
        String emailBody = "Hi regional managers. " + MANAGER_MESSAGE;
        List<String> emails = getEmailsForRole(Role.REGIONAL_MANAGER);
        this.emailService.sendMail(emailSubject, emailBody, emails);
    }

    private void sendNewOrderEmailTo(Order order, String email) {
        String emailSubject = "New order";
        String emailBody = "Hi " + order.getMadeBy().getFirstname() +
                ". Thank you for creating an " +
                "order with Acme.inc. The total of you order is " +
                order.getTotal();
        this.emailService.sendMail(emailSubject, emailBody, ImmutableList.of(email));
    }

    private List<String> getEmailsForRole(final Role role) {
        return this.userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(u -> u.getEmail())
                .collect(Collectors.toList());
    }

    public boolean isRegionalManagerThresholdReachedForToday(Order order) {
        double objective = this.salesReportRepository
                .getRegionalSalesObjective(
                    order.getLocation().getRegion());
        Collection<Order> allOrdersForRegion = this.orderRepository.findAll(new Date(), order.getLocation().getRegion());
        return hasThresholdJustBeenReached(order, allOrdersForRegion, objective);
    }

    public boolean isLocalManagerThresholdReachedForToday(Order order) {
        double objective = this.salesReportRepository
                .getLocalSalesObjective(
                  order.getLocation());
        Collection<Order> allOrdersForLocation = this.orderRepository.findAll(new Date(), order.getLocation());
        return hasThresholdJustBeenReached(order, allOrdersForLocation, objective);
    }

    private boolean hasThresholdJustBeenReached(Order order, Collection<Order> orders, double objective) {
        double total = orders.stream().mapToDouble(o -> o.getTotal()).sum();
        double totalMinusOrder = total - order.getTotal();
        return total >= objective && totalMinusOrder < objective;
    }

}
