package com.starempires.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
@RequiredArgsConstructor
public class JsonStarEmpiresDAO extends StarEmpiresDAO {
    private final String sessionDir;

    String loadItem(final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionDir, session, filename);
        return Files.readString(path);
    }

    String saveItem(final String content, final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionDir, session, filename);
        return Files.writeString(path, content).toAbsolutePath().toString();
    }
}