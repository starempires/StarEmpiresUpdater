package com.starempires;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.starempires.objects.Empire;
import com.starempires.updater.Phase;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class TurnNews {
    private final Map<Empire, Multimap<Phase, String>> news = Maps.newHashMap();

    public void addNews(final Phase phase, final Collection<Empire> empires, final String text) {
        empires.forEach(empire -> addNews(phase, empire, text));
    }

    public void addNews(final Phase phase, final Empire empire, final String text) {
        if (StringUtils.isNotBlank(text)) {
            final Multimap<Phase, String> phaseNews = news.computeIfAbsent(empire, e -> ArrayListMultimap.create());
            phaseNews.put(phase, text);
        }
    }

    public List<String> getEmpireNews(final Empire empire) {
        final Multimap<Phase, String> phaseNews = news.get(empire);
        final List<String> results = Lists.newArrayList();
        final List<Phase> phases = Lists.newArrayList(phaseNews.keySet());
        Collections.sort(phases);
        phases.forEach(phase -> results.addAll(phaseNews.get(phase)));
        return results;
    }
}