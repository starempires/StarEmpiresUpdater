package com.starempires.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class S3StarEmpiresDAO extends StarEmpiresDAO {
    private final S3Client s3Client;

    @Override
    String loadItem(final String session, final String filename) throws IOException {
        return "";
    }

    @Override
    String saveItem(final String content, final String session, final String filename) throws IOException {
        return "";
    }
}