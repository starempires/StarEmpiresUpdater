package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameters:
 *  LOAD ship1 [ship2 ….] ONTO carrier
 */
public class LoadShipPhaseUpdater extends PhaseUpdater {

    final private String CARGO_GROUP = "cargo";
    final private String CARRIER_GROUP = "carrier";
    final private Pattern PATTERN = Pattern.compile("^load (<"+ CARGO_GROUP+ ">) onto (<" + CARRIER_GROUP + ">)$", Pattern.CASE_INSENSITIVE);

    public LoadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.LOAD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.LOAD);
        orders.forEach(order -> {
            // load parameters are list of cargo handles to be loaded followed by carrier name last
            final Matcher matcher = PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                String carrierName = matcher.group(CARRIER_GROUP);
                final Ship carrier = turnData.getShip(carrierName);
                if (carrier == null) {
                    addNewsResult(order, order.getEmpire(), "Unknown carrier " + carrierName);
                }
                else {
                    String cargoString = matcher.group(CARGO_GROUP);
                    final List<String> cargoNames = Arrays.asList(cargoString.split( " "));
                    cargoNames.forEach(cargoName -> {
                        final Ship cargo = turnData.getShip(cargoName);
                        if (cargo == null) {
                            addNewsResult(order, "Unknown cargo %s".formatted(cargoName));
                        }
                        else if (!cargo.isSameSector(carrier)) {
                            addNewsResult(order, "Carrier %s and cargo %s are not in the same sector".formatted(carrier, cargo));
                        }
                        else if (!cargo.hasSameOwner(carrier)) {
                            addNewsResult(order, "Carrier %s and cargo %s have different owners".formatted(carrier, cargo));
                        }
                        else if (carrier.getEmptyRacks() < cargo.getTonnage()) {
                            addNewsResult(order, "Carrier %s has insufficient free racks to load cargo %s".formatted(carrier, cargo));
                        }
                        else {
                            turnData.load(cargo, carrier);
                            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(carrier);
                            addNewsResult(order, newsEmpires, "Ship " + cargo + " loaded onto carrier " + carrier);
                        }
                    });
                }
            }
        });
    }
}