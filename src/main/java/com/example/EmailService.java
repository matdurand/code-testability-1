package com.example;


import java.util.Collection;

public interface EmailService {
    public void sendMail(String subject, String body, Collection<String> emails);

}
