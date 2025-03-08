package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Empire;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Log4j2
public class OrderParser {

    private static final String ARG_SESSION_NAME = "session";
    private static final String ARG_TURN_NUMBER = "turn";
    private static final String ARG_SESSION_LOCATION = "sessionlocation";
    private static final String ARG_EMPIRE = "empire";

    private final StarEmpiresDAO dao;
    private final String sessionName;
    private final int turnNumber;
    private final String empireName;

    private static CommandLine extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt(ARG_SESSION_NAME).hasArg().desc("session name").required().build());
            options.addOption(Option.builder("t").argName("turn number").longOpt(ARG_TURN_NUMBER).hasArg().desc("turn number").required().build());
            options.addOption(Option.builder("e").argName("empire name").longOpt(ARG_EMPIRE).hasArg().desc("empire name").required().build());
            options.addOption(Option.builder("sl").argName("sessions locations").longOpt(ARG_SESSION_LOCATION).hasArg().desc("sessions location").required().build());

            final CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("OrderParser", options);
            throw e;
        }
    }

    private List<String> loadOrders() throws Exception {
        return dao.loadOrders(sessionName, empireName, turnNumber);
    }

    private TurnData loadTurnData() throws Exception {
        final TurnData turnData = dao.loadTurnData(sessionName, turnNumber);
        log.info("Loaded turn data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

    private Order parseOrder(final TurnData turnData, final Empire empire, final String text) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        final String[] tokens = text.trim().split(" ", 2);
        final String orderName = tokens[0];
        final String parameters = tokens[1];
        final OrderType orderType = EnumUtils.getEnumIgnoreCase(OrderType.class, orderName, OrderType.UNKNOWN);
        final Class<? extends Order> orderClass = orderType.getOrderClass();
        final Method method = orderClass.getMethod("parse", TurnData.class, Empire.class, String.class);
        return (Order) method.invoke(null, turnData, empire, parameters); // static method, so use `null` for instance
    }

    private List<Order> parseOrders(final TurnData turnData, final Empire empire, final List<String> ordersText) {
        final List<Order> orders = Lists.newArrayList();
        ordersText.stream()
                .filter(text -> !text.isBlank())
                .filter(text -> !text.startsWith("#"))
                .forEach(text -> {
                    try {
                        final Order order = parseOrder(turnData, empire, text);
                        orders.add(order);
                    } catch (Exception e) {
                        log.error("Error parsing {}", text, e);
                    }
                });
        return orders;
    }

    private List<String> formatResults(final List<Order> orders) {
        final List<String> results = Lists.newArrayList();
        orders.forEach(order -> {
            final List<String> messages = order.getResults();
            String result;
            if (messages.isEmpty()) {
                result = "%s: OK".formatted(order);
            }
            else if (messages.size() == 1) {
                result = "%s: %s".formatted(order, messages.get(0));
            }
            else {
                result = "%s:\n %s".formatted(order, StringUtils.join(messages, "\n "));
            }
            log.info(result);
            results.add(result);
        });
        return results;
    }

    private void saveOrders(final List<Order> orders) throws Exception {
        dao.saveReadyOrders(sessionName, empireName, turnNumber, orders);
        dao.saveOrderResults(sessionName, empireName, turnNumber, orders);
    }

    public OrderParser(final String sessionsLocation, final String sessionName, final String empireName, final int turnNumber) {
        this.sessionName = sessionName;
        this.empireName = empireName;
        this.turnNumber = turnNumber;
        dao = new S3StarEmpiresDAO(sessionsLocation, null);
    }

    public List<String> processOrders(final List<String> orders) throws Exception {
        final TurnData turnData = loadTurnData();
        final Empire empire = turnData.getEmpire(empireName);
        if (empire == null) {
            final String message = "Session %s does not contain empire %s".formatted(sessionName, empireName);
            log.error(message);
            throw new RuntimeException(message);
        }
        final List<Order> parsedOrders = parseOrders(turnData, empire, orders);
        saveOrders(parsedOrders);
        return formatResults(parsedOrders);
    }

    public static void main(final String[] args) {
        try {
            final CommandLine cmd = extractCommandLineOptions(args);
            final String sessionsLocation = cmd.getOptionValue(ARG_SESSION_LOCATION);
            final String sessionName = cmd.getOptionValue(ARG_SESSION_NAME);
            final String empireName = cmd.getOptionValue(ARG_EMPIRE);
            final int turnNumber = Integer.parseInt(cmd.getOptionValue(ARG_TURN_NUMBER));
            final OrderParser parser = new OrderParser(sessionsLocation, sessionName, empireName, turnNumber);
            final List<String> orders = parser.loadOrders();
            final List<String> results = parser.processOrders(orders);
            log.info("Processed %d orders for empire %s, session %s, turn %d"
                    .formatted(results.size(), empireName, sessionName, turnNumber));
        } catch (Exception exception) {
            log.error("Update failed", exception);
        }
    }

}