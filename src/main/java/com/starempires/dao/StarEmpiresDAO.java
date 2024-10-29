package com.starempires.dao;

import com.starempires.TurnData;

public interface StarEmpiresDAO {
    TurnData loadTurnData(final String session, final int turnNumber) throws Exception;
    void saveTurnData(final TurnData turnData) throws Exception;
}