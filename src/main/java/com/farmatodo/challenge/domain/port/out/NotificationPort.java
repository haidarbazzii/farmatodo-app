package com.farmatodo.challenge.domain.port.out;

public interface NotificationPort {
    void sendEmail(String to, String subject, String body);
}
