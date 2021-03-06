package com.example;

import java.util.Collection;
import java.util.Date;

public interface OrderRepository {
    Collection<Order> findAll(Date date, Location location);

    Collection<Order> findAll(Date date, Region region);
}
