package com.ridebooking.notification.service.interfaces;

public interface EmailService 
{
    void sendEmail(String to, String subject, String bodyHtml, String bodyText) throws Exception;
}
