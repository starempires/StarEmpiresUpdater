package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
import com.starempires.objects.HullType;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateDesignsPhaseUpdater extends PhaseUpdater {

    private static final String NAME_GROUP = "name";
    private static final String WORLD_GROUP = "world";
    private static final String HULL_TYPE_GROUP = "hulltype";
    private static final String GUNS_GROUP = "guns";
    private static final String ENGINES_GROUP = "engines";
    private static final String SCAN_GROUP = "scan";
    private static final String DP_GROUP = "dp";
    private static final String RACKS_GROUP = "racks";
    private static final String TONNAGE_GROUP = "tonnage";
    private static final String DESIGN_MISSILE_REGEX = "^design\\s+(?<" + NAME_GROUP + ">\\w+)\\s++(?<" + GUNS_GROUP + ">\\d+)\\s+(?<" + TONNAGE_GROUP + ">\\d+)\\s+";
    private static final String DESIGN_SHIP_REGEX = "^design\\s+(?<" + NAME_GROUP + ">\\w+)\\s+(?<" + WORLD_GROUP + ">\\w+)\\s+(?<" + HULL_TYPE_GROUP + ">\\w+)\\s+(?<" + GUNS_GROUP + ">\\d+)\\s+(?<"+ DP_GROUP + ">\\d+)\\s+(?<"+ ENGINES_GROUP + ">\\d+)\\s+(?<"+ SCAN_GROUP + ">\\d+)\\s+(?<"+ RACKS_GROUP + ">\\d+)$";
    private static final Pattern DESIGN_MISSILE_PATTERN = Pattern.compile(DESIGN_MISSILE_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern DESIGN_SHIP_PATTERN = Pattern.compile(DESIGN_SHIP_REGEX, Pattern.CASE_INSENSITIVE);

    public static final float DESIGN_MULTIPLIER = 0.5f;

    public CreateDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.CREATE_DESIGNS, turnData);
    }

    private void designMissile(final Order order, final String className, final int guns, final int tonnage) {
        final HullParameters hdp = turnData.getHullParameters(HullType.MISSILE);
        final int cost = hdp.getCost(guns, tonnage);
        final Empire empire = order.getEmpire();

        final ShipClass shipClass = ShipClass.builder()
                .name(className)
                .guns(guns)
                .dp(1)
                .engines(0)
                .scan(0)
                .racks(0)
                .tonnage(tonnage)
                .cost(cost)
                .ar(0)
                .hullType(HullType.MISSILE)
                .build();
        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        addNewsResult(order, "You have designed new missile class " + className);
    }

    private void designShip(final Order order, final String className, final String worldName,
                            final String hullTypeName, final int guns, final int dp,
                            final int engines, final int scan, final int racks) {
        final HullType hullType = EnumUtils.getEnum(HullType.class, hullTypeName.toUpperCase());
        final HullParameters hdp = turnData.getHullParameters(hullType);
        final int cost = hdp.getCost(guns, dp, engines, scan, racks);
        final int tonnage = hdp.getTonnage(guns, dp, engines, scan, racks);
        final double arMultiplier = hdp.getArMultiplier();
        final int ar = (int) Math.ceil(dp * arMultiplier);
        final int fee = (int) Math.ceil(cost * DESIGN_MULTIPLIER);

        final World world = turnData.getWorld(worldName);
        final Empire empire = order.getEmpire();
        if (!world.isOwnedBy(empire)) {
            addNewsResult(order, empire, "You do not own world " + worldName);
            return;
        }
        if (world.isInterdicted()) {
            addNewsResult(order, empire, "World " + worldName + " is interdicted; no designs possible.");
        }

        int stockpile = world.getStockpile();
        if (fee <= stockpile) {
            addNewsResult(order, "World " + worldName + " has insufficient stockpile to pay design fee " + fee + " RU.");
            return;
        }

        final ShipClass shipClass = ShipClass.builder()
                            .name(className)
                            .guns(guns)
                            .dp(dp)
                            .engines(engines)
                            .scan(scan)
                            .racks(racks)
                            .tonnage(tonnage)
                            .cost(cost)
                            .ar(ar)
                            .hullType(HullType.MISSILE)
                            .build();

        turnData.addShipClass(shipClass);
        empire.addKnownShipClass(shipClass);
        stockpile = world.adjustStockpile(-fee);
        addNewsResult(order, "You have designed new " + hullTypeName + " class " + className
                            + " (" + fee + " fee; " + stockpile + " RU remaining).");
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DESIGN);
        orders.forEach(order -> {
            final Matcher missileMatcher = DESIGN_MISSILE_PATTERN.matcher(order.getParametersAsString());
            final Matcher shipMatcher = DESIGN_SHIP_PATTERN.matcher(order.getParametersAsString());;
            if (missileMatcher.matches()) {
                final String className = missileMatcher.group(NAME_GROUP);
                final int guns = Integer.parseInt(missileMatcher.group(GUNS_GROUP));
                final int tonnage = Integer.parseInt(missileMatcher.group(TONNAGE_GROUP));
                designMissile(order, className, guns, tonnage);
            } else if (shipMatcher.matches()) {
                final String className = shipMatcher.group(NAME_GROUP);
                final String worldName = shipMatcher.group(WORLD_GROUP);
                final String hullTypeName = shipMatcher.group(HULL_TYPE_GROUP);
                final int guns = Integer.parseInt(shipMatcher.group(GUNS_GROUP));
                final int dp = Integer.parseInt(shipMatcher.group(DP_GROUP));
                final int engines = Integer.parseInt(shipMatcher.group(ENGINES_GROUP));
                final int scan = Integer.parseInt(shipMatcher.group(SCAN_GROUP));
                final int racks = Integer.parseInt(shipMatcher.group(RACKS_GROUP));
                designShip(order, className, worldName, hullTypeName, guns, dp, engines, scan, racks);
            }
            // TODO unknown orders
        });
    }
}