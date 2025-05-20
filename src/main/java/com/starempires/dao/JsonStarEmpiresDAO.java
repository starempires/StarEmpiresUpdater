package com.starempires.dao;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
public class JsonStarEmpiresDAO extends StarEmpiresDAO {

    public JsonStarEmpiresDAO(final String sessionsLocation, final String gameDataLocation) {
        super(sessionsLocation, gameDataLocation);
    }

    protected String loadSessionData(final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionsLocation, session, filename);
        return Files.readString(path);
    }

    protected boolean doesSessionDataExist(final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionsLocation, session, filename);
        return Files.exists(path);
    }

    protected String saveSessionData(final String data, final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionsLocation, session, filename);
        return Files.writeString(path, data).toAbsolutePath().toString();
    }

    protected void removeSessionData(final String session, final String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(sessionsLocation, session, filename);
        Files.delete(path);
    }

    @Override
    public String loadGameData(String filename) throws IOException {
        final Path path = FileSystems.getDefault().getPath(gameDataLocation, filename);
        return Files.readString(path);
    }
}