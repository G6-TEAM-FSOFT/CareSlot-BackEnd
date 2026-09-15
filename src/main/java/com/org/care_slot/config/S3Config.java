package com.org.care_slot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region:ap-southeast-2}")
    private String region;

    @Value("${aws.access-key:dummy-access-key}")
    private String accessKey;

    @Value("${aws.secret-key:dummy-secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        String effectiveAccessKey = (accessKey != null && !accessKey.isBlank()) ? accessKey : "dummy-access-key";
        String effectiveSecretKey = (secretKey != null && !secretKey.isBlank()) ? secretKey : "dummy-secret-key";
        String effectiveRegion = (region != null && !region.isBlank()) ? region : "ap-southeast-2";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                effectiveAccessKey,
                effectiveSecretKey);

        return S3Client.builder()
                .region(Region.of(effectiveRegion))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials))
                .build();
    }
}