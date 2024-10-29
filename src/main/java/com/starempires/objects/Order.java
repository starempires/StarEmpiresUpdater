package com.starempires.objects;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
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

    public List<String> getParameterSubList(final int startIndex) {
        return parameters.subList(startIndex, parameters.size());
    }

    public boolean getBooleanParameter(final int index) {
        return Boolean.parseBoolean(parameters.get(index));
    }

    @Override
    public String toString() {
        return orderType + " " + StringUtils.join(parameters, " ");
    }
}
