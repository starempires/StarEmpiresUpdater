package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Getter
@JsonPropertyOrder(alphabetic = true)
public class SectorShipSnapshot {
    final private int count;
    final private int tonnage;
    final private Map<String, ShipSnapshot> ships;
}