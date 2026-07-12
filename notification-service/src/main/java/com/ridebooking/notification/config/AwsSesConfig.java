package com.ridebooking.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public SesClient sesClient() 
    {
        if (accessKeyId == null || accessKeyId.isEmpty() ||
                secretAccessKey == null || secretAccessKey.isEmpty()) 
        {
            throw new IllegalStateException(
                    "AWS credentials not configured. Please set aws.access-key-id and aws.secret-access-key " +
                            "in application.properties or as environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY"
            );
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
