package com.ridebooking.notification.service.impl;

import com.ridebooking.notification.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

@Service
public class SesEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SesEmailService.class);

    private final SesClient sesClient;
    private final String from;

    public SesEmailService(SesClient sesClient, @Value("${aws.ses.from}") String from) {
        this.sesClient = sesClient;
        this.from = from;
    }

    @Override
    public void sendEmail(String to, String subject, String bodyHtml, String bodyText) throws Exception 
    {
        Destination destination = Destination.builder().toAddresses(to).build();
        Content subjectContent = Content.builder().data(subject).build();
        Content htmlContent = Content.builder().data(bodyHtml == null ? "" : bodyHtml).build();
        Content textContent = Content.builder().data(bodyText == null ? "" : bodyText).build();

        Body body = Body.builder().html(htmlContent).text(textContent).build();
        Message message = Message.builder().subject(subjectContent).body(body).build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(from)
                .destination(destination)
                .message(message)
                .build();

        log.debug("Sending email to {} with subject {}", to, subject);
        SendEmailResponse result = sesClient.sendEmail(request);
        log.debug("SES messageId={}", result.messageId());
    }
}
