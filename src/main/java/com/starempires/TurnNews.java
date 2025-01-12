package com.starempires;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.starempires.objects.Empire;
import com.starempires.updater.Phase;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@Getter
public class TurnNews {
    private final Map<Phase, Multimap<Empire, String>> news;

    public TurnNews() {
        news = Maps.newHashMap();
        for (Phase phase : Phase.values()) {
            news.put(phase, ArrayListMultimap.create());
        }
    }

    public void addNews(final Phase phase, final Collection<Empire> empires, final String text) {
        empires.forEach(empire -> addNews(phase, empire, text));
    }

    public void addNews(final Phase phase, final Empire empire, final String text) {
        if (StringUtils.isNotBlank(text)) {
            final Multimap<Empire, String> phaseNews = news.get(phase);
            phaseNews.put(empire, text);
        }
    }

    public void dump() {
        final List<Phase> phases = Lists.newArrayList(news.keySet());
        Collections.sort(phases);
        phases.forEach(phase -> {
            final Multimap<Empire, String> mapList = news.get(phase);
            for (Map.Entry<Empire, Collection<String>> entry : mapList.asMap().entrySet()) {
                final Empire empire = entry.getKey();
                final Collection<String> texts = entry.getValue();
                for (final String text : texts) {
                    log.info("{}:{}: {}", phase, empire, text);
                }
            }
        });
    }
}