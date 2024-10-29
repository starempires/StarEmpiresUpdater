package com.starempires;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.starempires.objects.Empire;
import com.starempires.phases.Phase;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
public class TurnNews {
    private final Map<Phase, Multimap<Empire, String>> news_;

    public Map<Phase, Multimap<Empire, String>> getNews() {
        return news_;
    }

    public TurnNews() {
        news_ = Maps.newHashMap();
        for (Phase phase : Phase.values()) {
            news_.put(phase, ArrayListMultimap.create());
        }
    }

    public void addNews(final Phase phase, final Collection<Empire> empires, final String text) {
        empires.forEach(empire -> addNews(phase, empire, text));
    }

    public void addNews(final Phase phase, final Empire empire, final String text) {
        if (StringUtils.isNotBlank(text)) {
            final Multimap<Empire, String> phaseNews = news_.get(phase);
            phaseNews.put(empire, text);
        }
    }

    public void dump() {
        final List<Phase> phases = Lists.newArrayList(news_.keySet());
        Collections.sort(phases);
        phases.forEach(phase -> {
            final Multimap<Empire, String> mapList = news_.get(phase);
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
