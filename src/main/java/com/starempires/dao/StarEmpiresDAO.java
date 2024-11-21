package com.starempires.dao;

import com.starempires.TurnData;
import com.starempires.orders.Order;

import java.util.List;

public interface StarEmpiresDAO {
    TurnData loadTurnData(final String session, final int turnNumber) throws Exception;
    void saveTurnData(final TurnData turnData) throws Exception;
    List<? extends Order> loadReadyOrders(final String session, final String empire, final int turnNumber) throws Exception;
    void saveReadyOrders(final String session, final String empire, final int turnNumber, final List<Order> orders) throws Exception;
}