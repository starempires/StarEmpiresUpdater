package com.starempires.objects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Order {

    /** id for this order */
    private final int id;
    /** empire who gave this order */
    private final Empire empire;
    /** type of this order */
    private final OrderType orderType;
    /** parameters associated with this order */
    private final ArrayList<String> parameters;
    /** text results from processing this order */
    private final List<String> results = Lists.newArrayList();
    /** was this order generated "synthetically", i.e., by the updater */
    private final boolean synthetic;

    public void addResult(final String text) {
        results.add(text);
    }

    public String getResultText() {
        return StringUtils.join(results);
    }

    public int getIntParameter(final int index) {
        return Integer.parseInt(parameters.get(index));
    }

    public String getStringParameter(final int index) {
        return parameters.get(index);
    }

    public List<String> getParameterSubList(final int startIndex) {
        return getParameterSubList(startIndex, parameters.size());
    }

    public List<String> getParameterSubList(final int startIndex, final int endIndex) {
        return parameters.subList(startIndex, endIndex);
    }

    public boolean getBooleanParameter(final int index) {
        return Boolean.parseBoolean(parameters.get(index));
    }

    @Override
    public String toString() {
        return orderType + " " + StringUtils.join(parameters, " ");
    }

    public int indexOfIgnoreCase(final String token) {
        return parameters.indexOf(StringUtils.lowerCase(token));
    }
}