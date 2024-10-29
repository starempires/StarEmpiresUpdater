package com.starempires.dao;

import com.starempires.TurnData;

public interface StarEmpiresDAO {
    TurnData loadData(final String session, final int turnNumber) throws Exception;
    void saveData(final TurnData turnData) throws Exception;
}
