package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
public abstract class OwnableObject extends MappableObject {

    public static final ObjectOwnerComparator OWNER_COMPARATOR = new ObjectOwnerComparator();

    public static class ObjectOwnerComparator implements Comparator<OwnableObject> {
        @Override
        public int compare(@NonNull final OwnableObject o1, @NonNull final OwnableObject o2) {
            return Objects.compare(o1.getOwner(), o2.getOwner(), (e1, e2) -> {
                if (e1 == null) {
                    return e2 == null ? 0 : -1;
                }
                else if (e2 == null) {
                    return 1;
                }
                else {
                    return StringUtils.compare(e1.getName(), e2.getName());
                }
            });
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = DeferredIdentifiableObjectDeserializer.class)
    protected Empire owner;

    protected OwnableObject(final String name, final Coordinate coordinate, final Empire owner) {
        super(name, coordinate);
        this.owner = owner;
    }

    public boolean isOwnedBy(final Empire empire) {
        return Objects.equals(owner, empire);
    }
}