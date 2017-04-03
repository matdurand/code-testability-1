package com.example;

public interface SalesReportRepository {
    int getLocalSalesObjective(Location location);
    int getRegionalSalesObjective(Region region);
}
