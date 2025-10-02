package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
import com.starempires.objects.HullType;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import com.starempires.util.StarEmpiresUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.EnumUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class DesignOrder extends WorldBasedOrder {

    //DESIGN world name hulltype parameters

    final static private String HULLTYPE_GROUP = "hulltype";
    final static private String PARAMETERS_GROUP = "parameters";
    final static private String HULLTYPE_CAPTURE_REGEX = "(?<" + HULLTYPE_GROUP + ">" + ID_REGEX + ")";
    final static private String SHIP_PARAMS_CAPTURE_REGEX = "(?<" + PARAMETERS_GROUP + ">[\\d\\s]+)";

    final static private String REGEX = WORLD_CAPTURE_REGEX + SPACE_REGEX + SHIP_CLASS_CAPTURE_REGEX + SPACE_REGEX + HULLTYPE_CAPTURE_REGEX + SPACE_REGEX + SHIP_PARAMS_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private String name;
    private HullType hullType;
    private int guns;
    private int dp;
    private int engines;
    private int scan;
    private int racks;
    private int tonnage;
    private int cost;
    private int designFee;
    private int ar;

    private boolean checkAttribute(final int value, final int min, final int max, final String name, final HullType hullType) {
        boolean rv = true;
        if (value < min) {
            addError(("%s designs must have at least %s".formatted(hullType, StarEmpiresUtils.plural(min, name))));
            rv = false;
        }
        if (value > max) {
            addError(("%s designs cannot have more than %s".formatted(hullType, StarEmpiresUtils.plural(max, name))));
            rv = false;
        }
        return rv;
    }

    public static DesignOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DesignOrder order = DesignOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESIGN)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(WORLD_GROUP);
            final String designName = matcher.group(SHIP_CLASS_GROUP);
            final String hullTypeName = matcher.group(HULLTYPE_GROUP);
            final String paramText = matcher.group(PARAMETERS_GROUP);

            final World world = turnData.getWorld(worldName);
            if (world == null || !empire.isKnownWorld(world)) {
                order.addError("Unknown world: " + worldName);
                order.setReady(false);
                return order;
            }
            if (!world.isOwnedBy(empire)) {
                order.addWarning(world, "You do not currently own world " + world);
            }
            if (world.isInterdicted()) {
                order.addWarning(world, "Currently interdicted");
            }
            if (turnData.isExistingShipClassName(designName)) {
                order.addError("Duplicate ship class name: " + designName);
                order.setReady(false);
                return order;
            }
            final HullType hullType = EnumUtils.getEnum(HullType.class, hullTypeName.toUpperCase());
            if (hullType == null) {
                order.addError("Unknown hull type: " + hullTypeName);
                order.setReady(false);
                return order;
            }
            order.world = world;
            order.name = designName;
            order.hullType = hullType;
            final HullParameters hullParameters = turnData.getHullParameters(order.hullType);
            final String[] tokens = paramText.split(" ");
            if (hullType == HullType.MISSILE) {
                if (tokens.length != 2) {
                    order.addError("Incorrect number of parameters for missile design: " + parameters);
                    order.setReady(false);
                    return order;
                }
                final int guns = Integer.parseInt(tokens[0]);
                final int tonnage = Integer.parseInt(tokens[1]);
                if (!order.checkAttribute(guns, 1, Integer.MAX_VALUE, "guns", hullType) ||
                    !order.checkAttribute(tonnage, 1, Integer.MAX_VALUE, "tonnage", hullType)) {
                    return order;
                }
                final int cost = hullParameters.getCost(guns, tonnage);
                order.addResult("  pending design for %s (missile) guns:%d tonnage:%d cost:%d".formatted(designName, guns, tonnage, cost));
                order.guns = guns;
                order.tonnage = tonnage;
                order.dp = 1;
                order.cost = cost;
                final ShipClass shipClass = ShipClass.builder()
                        .name(designName)
                        .hullType(hullType)
                        .guns(guns)
                        .tonnage(tonnage)
                        .cost(cost)
                        .build();
                turnData.addShipClass(shipClass);
                empire.addKnownShipClass(shipClass);
            }
            else {
                if (tokens.length != 5) {
                    order.addError("Incorrect number of parameters for ship design: " + parameters);
                    return order;
                }
                final int guns = Integer.parseInt(tokens[0]);
                final int dp = Integer.parseInt(tokens[1]);
                final int engines = Integer.parseInt(tokens[2]);
                final int scan = Integer.parseInt(tokens[3]);
                final int racks = Integer.parseInt(tokens[4]);
                if (!order.checkAttribute(guns, 0, hullParameters.getMaxGuns(), "guns", hullType) ||
                    !order.checkAttribute(dp, 1, hullParameters.getMaxDp(), "DP", hullType) ||
                    !order.checkAttribute(engines, 0, hullParameters.getMaxEngines(), "engines", hullType) ||
                    !order.checkAttribute(scan, 0, hullParameters.getMaxScan(), "scan", hullType) ||
                    !order.checkAttribute(racks, 0, hullParameters.getMaxRacks(), "racks", hullType)) {
                    return order;
                }
                final int cost = hullParameters.getCost(guns, dp, engines, scan, racks);
                final int designFee = Math.max(1, Math.round(cost * Constants.DEFAULT_DESIGN_MULTIPLIER));
                if (designFee > world.getStockpile()) {
                    order.addError(world, "Insufficient stockpile (%d) to pay %s design fee (%d)".formatted(world.getStockpile(), hullType, designFee));
                    return order;
                }
                final int tonnage = hullParameters.getTonnage(guns, dp, engines, scan, racks);
                final int ar = Math.max(1, Math.round(dp * Constants.DEFAULT_AUTO_REPAIR_MULTIPLIER));
                world.adjustStockpile(-designFee);
                order.addResult("Pending design for %s (%s) G:%d DP:%d E:%d S:%d R:%d".formatted(designName, hullType, guns, dp, engines, scan, racks));
                order.addResult("Cost: %d".formatted(cost));
                order.addResult("AR: %d".formatted(ar));
                order.addResult("Tonnage: %d".formatted(tonnage));
                order.addResult("Design fee %d (%d remaining)".formatted(designFee, world.getStockpile()));;
                order.guns = guns;
                order.dp = dp;
                order.engines = engines;
                order.scan = scan;
                order.racks = racks;
                order.cost = cost;
                order.designFee = designFee;
                order.tonnage = tonnage;
                order.ar = ar;
                final ShipClass shipClass = ShipClass.builder()
                        .name(designName)
                        .hullType(hullType)
                        .guns(guns)
                        .dp(dp)
                        .engines(engines)
                        .scan(scan)
                        .racks(racks)
                        .cost(cost)
                        .ar(ar)
                        .tonnage(tonnage)
                        .build();
                turnData.addShipClass(shipClass);
                empire.addKnownShipClass(shipClass);
            }
            order.setReady(true);
        }
        else {
            order.addError("Invalid DESIGN order: " + parameters);
        }
        return order;
    }

    public static DesignOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = DesignOrder.builder();
        WorldBasedOrder.parseReady(node, turnData, OrderType.DESIGN, builder);
        return builder
                .name(getString(node, "name"))
                .hullType(EnumUtils.getEnumIgnoreCase(HullType.class, getString(node, "hullType")))
                .guns(getInt(node, "guns"))
                .dp(getInt(node, "dp"))
                .engines(getInt(node, "engines"))
                .scan(getInt(node, "scan"))
                .racks(getInt(node, "racks"))
                .tonnage(getInt(node, "tonnage")) // for missiles
                .build();
    }
}