package com.ridebooking.ride.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsSqsConfig {

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Bean
    public SqsClient sqsClient() {
        AwsCredentialsProvider credentialsProvider =
                (accessKeyId != null && !accessKeyId.isBlank()
                        && secretAccessKey != null && !secretAccessKey.isBlank())
                        ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                        : DefaultCredentialsProvider.create();

        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
