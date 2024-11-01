package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import org.apache.commons.lang3.StringUtils;

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

    private final Pattern PATTERN = Pattern.compile("^load (<cargo>\\w+) onto (<carrier>\\w+)$");

    public LoadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.LOAD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.LOAD);
        orders.forEach(order -> {
            // load parameters are list of cargo handles to be loaded followed by carrier name last
            final List<String> parameters = order.getParameters();
            final Matcher matcher = PATTERN.matcher(StringUtils.join(parameters, " "));
            if (matcher.matches()) {
                String cargoString = matcher.group("cargo");
                String carrierName = matcher.group("carrier");
                final Ship carrier = turnData.getShip(carrierName);
                if (carrier == null) {
                    addNewsResult(order, order.getEmpire(), "Unknown carrier " + carrierName);
                }
                else {
                    final List<String> cargoNames = Arrays.asList(cargoString.split( " "));
                    cargoNames.forEach(cargoName -> {
                        final Ship cargo = turnData.getShip(cargoName);
                        if (cargo.isSameSector(carrier)) {
                            if (cargo.hasSameOwner(carrier)) {
                                if (carrier.getEmptyRacks() >= cargo.getTonnage()) {
                                    turnData.load(cargo, carrier);
                                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(carrier);
                                    addNewsResult(order, newsEmpires, "Ship " + cargo + " loaded onto carrier " + carrier);

                                }
                                else {
                                    addNewsResult(order, order.getEmpire(),
                                            "Carrier " + carrier + " has insufficient free racks to load cargo " + cargo);
                                }
                            }
                            else {
                                addNewsResult(order, order.getEmpire(), "Carrier " + carrier + " and cargo " + cargo
                                        + " have different owners");
                            }
                        }
                        else {
                            addNewsResult(order, order.getEmpire(),
                                    "Carrier " + carrier + " and cargo " + cargo + " are not in same sector.");
                        }
                    });
                }
            }
        });
    }
}