package com.example;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

public class OrdersNotifierV2 {
    private final UserRepository userRepository;
    private final SalesThresholdResolver salesThresholdResolver;
    private final EmailService emailService;

    private static final String MANAGER_EMAIL_SUBJECT = "Sales are now above daily objectives";
    private static final String MANAGER_MESSAGE = "Sales for today have reached the expected threshold.";

    public OrdersNotifierV2(
            UserRepository userRepository,
            SalesThresholdResolver salesThresholdResolver,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.salesThresholdResolver = salesThresholdResolver;
        this.emailService = emailService;
    }

    public void onNewOrder(Order order) {
        sendNewOrderEmailTo(order, order.getMadeBy().getEmail());
        if (this.salesThresholdResolver.isLocalSalesThresholdMetWithNewOrder(order)) {
            sendDailySalesOKToLocalManagers();
        }
        if (this.salesThresholdResolver.isRegionalSalesThresholdMetWithNewOrder(order)) {
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
}
