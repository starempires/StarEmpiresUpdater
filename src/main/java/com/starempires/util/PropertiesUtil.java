package com.starempires.util;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Log4j2
public class PropertiesUtil extends HashMap<String, String> {

    public PropertiesUtil(final @NonNull List<String> data) throws IOException {
        data.forEach(line -> {
            final String[] parts = line.toLowerCase().split("=");
            if (parts.length == 2) {
                put(parts[0].trim(), parts[1].trim());
            }
            else {
                log.warn("Invalid line in properties file: {}", line);
            }
        });
    }

    public int getInt(final String key) throws NumberFormatException {
        return getInt(key, 0);
    }

    public int getInt(final String key, final int defaultValue) throws NumberFormatException {
        int rv = defaultValue;
        final String value = get(key.toLowerCase());
        if (value != null) {
            rv = Integer.parseInt(value);
        }
        return rv;
    }

    public String getString(final String key) {
        return getString(key, null);
    }

    public String getString(final String key, final String defaultValue) {
        return ObjectUtils.firstNonNull(get(key.toLowerCase()), defaultValue);
    }

    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        boolean rv = defaultValue;
        final String value = get(key.toLowerCase());
        if (value != null) {
            rv = Boolean.parseBoolean(value);
        }
        return rv;
    }
}