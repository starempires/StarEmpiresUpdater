package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@SuperBuilder
public abstract class Order {


    /** empire who gave this order */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected final Empire empire;
    protected final String parameters;
    /** text results from processing this order */
    @JsonIgnore
    protected final List<String> results = Lists.newArrayList();
    /** was this order generated "synthetically", i.e., by the updater */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected final boolean synthetic;
    protected final OrderType orderType;
    protected boolean ready = true;

    public void addResult(final String text) {
        results.add(text);
    }

    public void addWarning(final IdentifiableObject object, final String text) {
        results.add("%s: Warning: %s".formatted(object, text));
    }

    public void addError(final Object object, final String text) {
        results.add("%s: Error: %s".formatted(object, text));
    }

    public void addError(final String text) {
        results.add("Error: %s".formatted(text));
    }

    public void addOKResult(final Object object) {
        results.add("%s: OK".formatted(object));
    }

    @JsonIgnore
    public String getResultText() {
        return StringUtils.join("\\n", results);
    }

    //TODO remove
    @JsonIgnore
    public String getParametersAsString() {
        return parameters;
    }

    public String toString() {
        return orderType.name() + " " + parameters;
    }

    public static Order parse(final TurnData turnData, final Empire empire, final String parameters) {
        throw new UnsupportedOperationException("Subclasses must implement this method");
    }
}