package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import com.starempires.objects.Empire;
import com.starempires.objects.HullParameters;
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
    private static final String ARG_SESSION_DIR = "sessiondir";
    private static final String ARG_EMPIRE = "empire";

    private final StarEmpiresDAO dao;
    private String sessionName;
    private int turnNumber;
    private String sessionDir;
    private String empireName;

    private final List<Class<? extends Order>> orderClasses = List.of(UnloadOrder.class, LoadOrder.class);

    private void extractCommandLineOptions(final String[] args) throws ParseException {
        final Options options = new Options();
        try {
            options.addOption(Option.builder("s").argName("session name").longOpt(ARG_SESSION_NAME).hasArg().desc("session name").required().build());
            options.addOption(Option.builder("t").argName("turn number").longOpt(ARG_TURN_NUMBER).hasArg().desc("turn number").required().build());
            options.addOption(Option.builder("e").argName("empire name").longOpt(ARG_EMPIRE).hasArg().desc("empire name").required().build());
            options.addOption(Option.builder("sd").argName("session dir").longOpt(ARG_SESSION_DIR).hasArg().desc("session dir").required().build());

            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(options, args);
            sessionName = cmd.getOptionValue(ARG_SESSION_NAME);
            turnNumber = Integer.parseInt(cmd.getOptionValue(ARG_TURN_NUMBER));
            empireName = cmd.getOptionValue(ARG_EMPIRE);
            sessionDir = cmd.getOptionValue(ARG_SESSION_DIR);
        } catch (ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Updater", options);
            throw e;
        }
    }

    private TurnData loadTurnData() throws Exception {
        final TurnData turnData = dao.loadTurnData(sessionName, turnNumber);
        log.info("Loaded turn data for session {}, turn {}", sessionName, turnNumber);
        return turnData;
    }

    private List<HullParameters> loadHullParameters() throws Exception {
        final List<HullParameters> hullParameters = dao.loadHullParameters(sessionName);
        log.info("Loaded hull parameters for session {}", sessionName);
        return hullParameters;
    }

    private List<String> loadOrders() throws Exception {
        return dao.loadOrders(sessionName, empireName, turnNumber);
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

    private void saveOrders(final List<Order> orders) throws Exception {
        dao.saveReadyOrders(sessionName, empireName, turnNumber, orders);
        dao.saveOrderResults(sessionName, empireName, turnNumber, orders);
        orders.forEach(order -> {
            final List<String> messages = order.getResults();
            if (messages.isEmpty()) {
                log.info("{}: OK", order);
            }
            else if (messages.size() == 1) {
                log.info("{}: {}", order, messages.get(0));
            }
            else {
                log.info("{}:\n {}", order, StringUtils.join(messages, "\n "));
            }
        });
    }

    public OrderParser(final String[] args) throws ParseException {
        extractCommandLineOptions(args);
//        dao = new JsonStarEmpiresDAO(sessionDir, null);
        dao = new S3StarEmpiresDAO(sessionDir, null);
    }

    public static void main(final String[] args) {
        try {
            final OrderParser parser = new OrderParser(args);
            final TurnData turnData = parser.loadTurnData();
            final Empire empire = turnData.getEmpire(parser.empireName);
            if (empire == null) {
                final String message = "Session %s does not contain empire %s".formatted(parser.sessionName, parser.empireName);
                log.error(message);
                throw new RuntimeException(message);
            }
            final List<String> ordersTexts = parser.loadOrders();
            final List<Order> orders = parser.parseOrders(turnData, empire, ordersTexts);
            parser.saveOrders(orders);
        } catch (Exception exception) {
            log.error("Update failed", exception);
        }

    }

}