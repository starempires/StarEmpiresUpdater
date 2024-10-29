package com.starempires.objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an identifiable entity with a name
 * 
 * @author john
 *
 */
@RequiredArgsConstructor
@Data
@NoArgsConstructor(force = true)
public abstract class IdentifiableObject implements Comparable<IdentifiableObject> {

    protected final String name;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final @NonNull IdentifiableObject obj) {
        return StringUtils.compareIgnoreCase(name, obj.name);
    }
}
