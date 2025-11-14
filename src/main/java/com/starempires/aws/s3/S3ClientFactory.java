package com.starempires.aws.s3;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientFactory {
    public static S3Client createS3Client() {
        return S3Client.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }
}