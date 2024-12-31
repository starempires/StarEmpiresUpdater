package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.starempires.objects.Coordinate;
import com.starempires.objects.ScanStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Map;

@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
@Getter
@JsonPropertyOrder(alphabetic = true)
@EqualsAndHashCode(callSuper = true)
public class SectorSnapshot extends Coordinate {

    final private ScanStatus status;
    final private int lastTurnScanned;
    final private Map<String, SectorShipSnapshot> ships;
    final private int unidentifiedShipCount;
    final private int unidentifiedShipTonnage;
    final private WorldSnapshot world;
    final private Collection<PortalSnapshot> portals;
    final private Collection<StormSnapshot> storms;
    final private int row;
    final private int column;
}