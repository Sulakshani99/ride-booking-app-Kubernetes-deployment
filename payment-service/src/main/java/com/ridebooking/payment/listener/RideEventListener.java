package com.ridebooking.payment.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridebooking.payment.dto.RideEvent;
import com.ridebooking.payment.service.interfaces.IPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class RideEventListener {

    private static final Logger log = LoggerFactory.getLogger(RideEventListener.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final IPaymentService paymentService;
    private final String queueUrl;

    public RideEventListener(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            IPaymentService paymentService,
            @Value("${aws.sqs.payment-queue-url:}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.queueUrl = queueUrl;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollQueue() {
        if (queueUrl == null || queueUrl.isBlank()) {
            return;
        }

        List<Message> messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(10)
                        .build())
                .messages();

        for (Message message : messages) {
            try {
                RideEvent event = objectMapper.readValue(message.body(), RideEvent.class);
                paymentService.handleRideEvent(event);
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.error("Failed to process ride event; leaving on queue for retry", e);
            }
        }
    }
}