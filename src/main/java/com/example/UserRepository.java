package com.example;

import java.util.Collection;

public interface UserRepository {
    Collection<User> findAll();
    Collection<User> findAllAdmins();
}
