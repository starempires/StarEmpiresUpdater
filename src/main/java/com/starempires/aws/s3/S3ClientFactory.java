package com.starempires.aws.s3;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ClientFactory {
    public static S3Client createS3Client() {
        AwsCredentialsProvider credentialsProvider;

        if (System.getenv("AWS_EXECUTION_ENV") != null) {
            // Running inside AWS Lambda, use IAM role-based authentication
            credentialsProvider = InstanceProfileCredentialsProvider.create();
        } else {
            // Running on a desktop, use local credentials (~/.aws/credentials)
            credentialsProvider = ProfileCredentialsProvider.create("default");
        }

        return S3Client.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}