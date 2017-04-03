package com.example;

public interface SalesThresholdResolver {
    boolean isLocalSalesThresholdMetWithNewOrder(Order order);

    boolean isRegionalSalesThresholdMetWithNewOrder(Order order);
}
