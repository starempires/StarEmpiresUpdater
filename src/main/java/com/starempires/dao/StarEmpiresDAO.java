package com.starempires.dao;

import com.starempires.TurnData;
import com.starempires.generator.EmpireSnapshot;
import com.starempires.objects.HullParameters;
import com.starempires.orders.Order;

import java.util.List;
import java.util.Map;

public interface StarEmpiresDAO {
    List<String> loadEmpireData(final String session) throws Exception;
    List<String> loadEmpireNames(final String session) throws Exception;
    TurnData loadTurnData(final String session, final int turnNumber) throws Exception;
    void saveTurnData(final String session, final TurnData turnData) throws Exception;
    List<String> loadOrders(final String session, final String empire, final int turnNumber) throws Exception;
    List<Order> loadReadyOrders(final String session, final String empire, final int turnNumber, final TurnData turnData) throws Exception;
    void saveReadyOrders(final String session, final String empire, final int turnNumber, final List<Order> orders) throws Exception;
    void saveOrderResults(final String session, final String empire, final int turnNumber, final List<Order> orders) throws Exception;
    void saveSnapshot(final String session, final String empire, final int turnNumber, final EmpireSnapshot snapshot) throws Exception;
    List<HullParameters> loadHullParameters(final String session) throws Exception;
    void saveColors(final String session, final Map<String, String> colors) throws Exception;
    void saveNews(final String session, final String empire, final int turnNumber, List<String> news) throws Exception;
    Map<String, String> loadColors(final String session) throws Exception;
}