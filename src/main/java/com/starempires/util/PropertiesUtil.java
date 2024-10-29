package com.starempires.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Log4j2
public class PropertiesUtil {

    private final ListMultimap<String, String> properties = ArrayListMultimap.create();

    public PropertiesUtil(final @NonNull Path path) throws IOException {
        populate(path.toString());
    }

    public void populate(final @NonNull String filename) throws IOException {
        try (final FileReader reader = new FileReader(filename)) {
            final Properties props = new Properties();
            props.load(reader);
            props.forEach((key, value) -> properties.put(key.toString().toLowerCase(), value.toString()));
            log.info("Loaded properties from file {}", filename);
        } catch (IOException ex) {
            log.error("Error loading properties from file {}", filename, ex);
            throw ex;
        }
    }

    public void populate(final String[] values) {
        String key = null;

        for (String value : values) {
            if (value.startsWith("-")) {
                key = value.substring(1).toLowerCase();
            }
            else {
                if (key != null) {
                    properties.put(key, value);
                    log.info("Added properties {} {}", key, value);
                }
            }
        }
    }

    private String getFirst(final String key) {
        final List<String> values = properties.get(key.toLowerCase());
        String rv = null;
        if (CollectionUtils.isNotEmpty(values)) {
            rv = values.get(0);
        }
        return rv;
    }

    public int getInt(final String key) throws NumberFormatException {
        return getInt(key, 0);
    }

    public int getInt(final String key, final int defaultValue) throws NumberFormatException {
        int rv = defaultValue;
        final String value = getFirst(key);
        if (value != null) {
            rv = Integer.parseInt(value);
        }
        return rv;
    }

    public String getString(final String key) {
        return getString(key, null);
    }

    public String getString(final String key, final String defaultValue) {
        return ObjectUtils.firstNonNull(getFirst(key), defaultValue);
    }

    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        boolean rv = defaultValue;
        final String value = getFirst(key);
        if (value != null) {
            rv = Boolean.parseBoolean(value);
        }
        return rv;
    }

    public double getDouble(final String key) {
        return getDouble(key, 0.0);
    }

    public double getDouble(final String key, final double defaultValue) {
        double rv = defaultValue;
        final String value = getFirst(key);
        if (value != null) {
            rv = Double.parseDouble(value);
        }
        return rv;
    }

    public List<String> getList(final String option) {
        return ObjectUtils.firstNonNull(properties.get(option.toLowerCase()), Collections.emptyList());
    }
}
