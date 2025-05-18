package com.starempires.dao;

import com.starempires.aws.s3.S3ClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.Charset;


@Log4j2
@RequiredArgsConstructor
public class S3StarEmpiresDAO extends StarEmpiresDAO {
    private static final S3Client S3_CLIENT = S3ClientFactory.createS3Client();

    public S3StarEmpiresDAO(final String sessionsLocation, final String gameDataLocation) {
        super(sessionsLocation, gameDataLocation);
    }

    private String loadData(final String bucket, final String key) throws IOException {
        final GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try {
            final ResponseBytes<GetObjectResponse> objectBytes = S3_CLIENT.getObjectAsBytes(request);
            final byte[] data = objectBytes.asByteArray();
            return new String(data, Charset.defaultCharset());
        }
        catch (NoSuchKeyException ex) {
            log.error("No such key {}/{}", bucket, key);
            return null;
        }
    }

    private String saveData(final String bucket, final String key, final String data) throws IOException {
        final PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .contentType("text/plain")
                .key(key)
                .build();
        S3_CLIENT.putObject(request, RequestBody.fromString(data));
        return "s3://" + bucket + "/" + key;
    }

    @Override
    protected String loadSessionData(final String session, final String filename) throws IOException {
        final String key = session + "/" + filename;
        return loadData(sessionsLocation, key);
    }

    @Override
    public String loadGameData(final String filename) throws IOException {
        return loadData(gameDataLocation, filename);
    }

    @Override
    protected String saveSessionData(final String data, final String session, final String filename) throws IOException {
        final String key = session + "/" + filename;
        return saveData(sessionsLocation, key, data);
    }
}