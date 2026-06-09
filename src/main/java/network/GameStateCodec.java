package network;

import logic.Game;
import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.HiddenCard;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;
import model.PropertiesCardsType;
import model.PropertyColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class GameStateCodec {
    // Creates a GameStateCodec instance.
    private GameStateCodec() {
    }

    // Runs encode.
    public static String encode(Game game, int playerId) {
        return encode(game, playerId, -1);
    }

    // Runs encode with turn timer information.
    public static String encode(Game game, int playerId, int turnRemainingSeconds) {
        if (game == null) {
            return "NO_GAME";
        }

        StringBuilder builder = new StringBuilder();
        int playerIndex = playerId - 1;

        builder.append("you=").append(playerId);
        builder.append(";");
        builder.append("currentPlayer=").append(game.getCurrentPlayerIndex() + 1);
        builder.append(";turnRemaining=").append(turnRemainingSeconds);
        builder.append(";discardPhase=").append(game.isDiscard());
        appendPaymentState(builder, game, playerId);
        builder.append(";players=");

        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);

            if (i > 0) {
                builder.append(",");
            }

            builder.append("P").append(i + 1);
            String playerName = player.getName();
            if (playerName != null) {
                builder.append(playerName);
            }
            builder.append("(hand=").append(player.getHandCards().size());
            builder.append(",bank=").append(player.getBankCards().size());
            builder.append(",properties=").append(player.getPropertyCards().size());
            builder.append(",used=").append(player.getUseCardTimes());
            builder.append(")");
        }

        if (playerIndex >= 0 && playerIndex < game.getPlayers().size()) {
            builder.append(";yourHand=");
            appendCards(builder, game.getPlayers().get(playerIndex).getHandCards(), ",");
            builder.append(";yourBank=");
            appendCards(builder, game.getPlayers().get(playerIndex).getBankCards(), ",");
            builder.append(";yourProperties=");
            appendProperties(builder, game.getPlayers().get(playerIndex).getPropertyCards(), ",");
        }

        builder.append(";publicBanks=");
        appendPublicCardsByPlayer(builder, game, false);
        builder.append(";publicProperties=");
        appendPublicCardsByPlayer(builder, game, true);
        builder.append(";win=").append(game.isWin());

        return builder.toString();
    }

    // Runs decode.
    public static Snapshot decode(String body) {
        Snapshot snapshot = new Snapshot();
        Map<String, String> fields = splitFields(body);
        snapshot.you = parseInt(fields.get("you"));
        snapshot.currentPlayerIndex = Math.max(0, parseInt(fields.get("currentPlayer")) - 1);
        snapshot.turnRemainingSeconds = parseInt(fields.getOrDefault("turnRemaining", "-1"));
        snapshot.discard = Boolean.parseBoolean(fields.getOrDefault("discardPhase", "false"));
        snapshot.win = Boolean.parseBoolean(fields.getOrDefault("win", "false"));

        Map<Integer, PlayerSummary> playerSummaries = parsePlayerSummaries(fields.get("players"));
        List<Card> yourHand = parseCards(fields.get("yourHand"), ",");
        Map<Integer, List<Card>> banks = parseCardGroups(fields.get("publicBanks"));
        Map<Integer, List<Card>> properties = parseCardGroups(fields.get("publicProperties"));
        int playerCount = Math.max(Math.max(playerSummaries.size(), banks.size()), properties.size());

        DrawPileAndDiscardPile drawPile = new DrawPileAndDiscardPile();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player(drawPile);
            player.getHandCards().clear();
            player.getBankCards().clear();
            player.getPropertyCards().clear();
            if (i == snapshot.you) {
                player.getHandCards().addAll(yourHand);
            } else {
                int hiddenHandCount = playerSummaries.getOrDefault(i, PlayerSummary.EMPTY).handCount;
                for (int cardNumber = 0; cardNumber < hiddenHandCount; cardNumber++) {
                    player.getHandCards().add(new HiddenCard());
                }
            }
            player.getBankCards().addAll(banks.getOrDefault(i, List.of()));
            for (Card card : properties.getOrDefault(i, List.of())) {
                if (card instanceof PropertiesCards propertyCard) {
                    player.getPropertyCards().add(propertyCard);
                }
            }
            player.setUseCardTimes(playerSummaries.getOrDefault(i, PlayerSummary.EMPTY).usedCount);
            PlayerSummary summary = playerSummaries.getOrDefault(i, PlayerSummary.EMPTY);
            if (summary.name != null) {
                player.setName(summary.name);
            }
            snapshot.players.add(player);
        }

        snapshot.paymentRequest = parsePayment(fields.get("payment"), snapshot.players);
        return snapshot;
    }

    // Runs append payment state.
    private static void appendPaymentState(StringBuilder builder, Game game, int playerId) {
        builder.append(";payment=");

        if (!game.isPaymentSelecting()) {
            builder.append("none");
            return;
        }

        Game.PaymentRequest request = game.getCurrentPaymentRequest();
        int receiverId = game.getPlayers().indexOf(request.getReceiver()) + 1;
        int payerId = game.getPlayers().indexOf(request.getPayer()) + 1;
        int responderId = game.getPlayers().indexOf(request.getJustSayNoResponder()) + 1;
        int lastJustSayNoUserId = game.getPlayers().indexOf(request.getLastJustSayNoUser()) + 1;

        builder.append("payer=").append(payerId);
        builder.append(",receiver=").append(receiverId);
        builder.append(",amount=").append(request.getAmount());
        builder.append(",justSayNoPending=").append(request.isJustSayNoPending());
        builder.append(",justSayNoResponder=").append(responderId);
        builder.append(",lastJustSayNoUser=").append(lastJustSayNoUserId);

        int activeJustSayNoPlayerId = request.isJustSayNoPending() ? responderId : payerId;

        if (playerId == activeJustSayNoPlayerId) {
            builder.append(",youMustPay=true");
            builder.append(",canJustSayNo=").append(game.canCurrentPaymentUseJustSayNo());
        }
    }

    // Runs append public cards by player.
    private static void appendPublicCardsByPlayer(StringBuilder builder, Game game, boolean properties) {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i > 0) {
                builder.append("|");
            }

            Player player = game.getPlayers().get(i);
            builder.append("P").append(i + 1).append("[");

            if (properties) {
                appendProperties(builder, player.getPropertyCards(), "~");
            } else {
                appendCards(builder, player.getBankCards(), "~");
            }

            builder.append("]");
        }
    }

    // Runs append cards.
    private static void appendCards(StringBuilder builder, List<? extends Card> cards, String separator) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }

            builder.append(cardToText(cards.get(i)));
        }
    }

    // Runs append properties.
    private static void appendProperties(StringBuilder builder, List<PropertiesCards> cards, String separator) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }

            builder.append(propertyToText(cards.get(i)));
        }
    }

    // Runs card to text.
    private static String cardToText(Card card) {
        if (card instanceof MoneyCards) {
            return "MONEY:" + card.getValue();
        }

        if (card instanceof ActionCards actionCard) {
            return "ACTION:" + actionCard.getActionCardType().name() + ":" + card.getValue();
        }

        if (card instanceof PropertiesCards propertyCard) {
            return propertyToText(propertyCard);
        }

        return "CARD_" + card.getValue();
    }

    // Runs property to text.
    private static String propertyToText(PropertiesCards card) {
        String currentColor = card.getCurrentColor() == null ? "NO_COLOR" : card.getCurrentColor().name();
        return "PROPERTY:" + card.getType().name() + ":" + currentColor + ":"
                + card.getImageFileName() + ":" + card.getValue() + ":"
                + card.hasHouse() + ":" + card.hasHotel();
    }

    // Runs split fields.
    private static Map<String, String> splitFields(String body) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String part : body.split(";")) {
            int equals = part.indexOf('=');
            if (equals > 0) {
                fields.put(part.substring(0, equals), part.substring(equals + 1));
            }
        }
        return fields;
    }

    // Parses player hand and used-card summaries.
    private static final Pattern PLAYER_TOKEN = Pattern.compile("P(\\d+)(.*?)\\(");
    private static Map<Integer, PlayerSummary> parsePlayerSummaries(String text) {
        Map<Integer, PlayerSummary> result = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        int cursor = 0;
        while (cursor < text.length()) {
            int next = text.indexOf("),P", cursor);
            String token;
            if (next < 0) {
                token = text.substring(cursor);
                cursor = text.length();
            } else {
                token = text.substring(cursor, next + 1);
                cursor = next + 2;
            }
            java.util.regex.Matcher m = PLAYER_TOKEN.matcher(token);
            if (!m.find()) {
                continue;
            }
            int parsedPlayerId = parseInt(m.group(1));
            String name = m.group(2);
            if (name.isEmpty()) {
                name = null;
            }
            int open = m.end() - 1;
            int close = token.indexOf(')');
            int hand = 0;
            int used = 0;
            for (String item : token.substring(open + 1, close).split(",")) {
                String[] pair = item.split("=", 2);
                if (pair.length != 2) {
                    continue;
                }
                if ("hand".equals(pair[0])) {
                    hand = parseInt(pair[1]);
                } else if ("used".equals(pair[0])) {
                    used = parseInt(pair[1]);
                }
            }
            result.put(parsedPlayerId, new PlayerSummary(hand, used, name));
        }
        return result;
    }

    // Parses card groups.
    private static Map<Integer, List<Card>> parseCardGroups(String text) {
        Map<Integer, List<Card>> result = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        for (String token : text.split("\\|")) {
            int open = token.indexOf('[');
            int close = token.lastIndexOf(']');
            if (!token.startsWith("P") || open < 0 || close < open) {
                continue;
            }
            int parsedPlayerId = parseInt(token.substring(1, open));
            result.put(parsedPlayerId, parseCards(token.substring(open + 1, close), "~"));
        }
        return result;
    }

    // Parses cards.
    private static List<Card> parseCards(String text, String separator) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        ArrayList<Card> cards = new ArrayList<>();
        for (String raw : text.split(Pattern.quote(separator))) {
            Card card = parseCard(raw);
            if (card != null) {
                cards.add(card);
            }
        }
        return cards;
    }

    // Parses card.
    private static Card parseCard(String raw) {
        String[] parts = raw.split(":", -1);
        if (parts.length == 0) {
            return null;
        }
        if ("MONEY".equals(parts[0]) && parts.length >= 2) {
            return new MoneyCards(parseInt(parts[1]));
        }
        if ("ACTION".equals(parts[0]) && parts.length >= 2) {
            return new ActionCards(ActionCardType.valueOf(parts[1]));
        }
        if ("PROPERTY".equals(parts[0]) && parts.length >= 5) {
            PropertiesCardsType type = PropertiesCardsType.valueOf(parts[1]);
            PropertiesCards card = new PropertiesCards(type, type.name(), parts[3]);
            if (!"NO_COLOR".equals(parts[2])) {
                card.setCurrentColor(PropertyColor.valueOf(parts[2]));
            }
            if (parts.length >= 7) {
                card.setHasHouse(Boolean.parseBoolean(parts[5]));
                card.setHasHotel(Boolean.parseBoolean(parts[6]));
            }
            return card;
        }
        return null;
    }

    // Parses payment.
    private static Game.PaymentRequest parsePayment(String text, ArrayList<Player> players) {
        if (text == null || text.equals("none")) {
            return null;
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String token : text.split(",")) {
            String[] pair = token.split("=", 2);
            if (pair.length == 2) {
                values.put(pair[0], pair[1]);
            }
        }
        int payerIndex = parseInt(values.get("payer")) - 1;
        int receiverIndex = parseInt(values.get("receiver")) - 1;
        int amount = parseInt(values.get("amount"));
        if (payerIndex < 0 || receiverIndex < 0
                || payerIndex >= players.size() || receiverIndex >= players.size()) {
            return null;
        }
        Game.PaymentRequest request = new Game.PaymentRequest(players.get(receiverIndex), players.get(payerIndex), amount);
        if (Boolean.parseBoolean(values.getOrDefault("justSayNoPending", "false"))) {
            int responderIndex = parseInt(values.get("justSayNoResponder")) - 1;
            int lastUserIndex = parseInt(values.get("lastJustSayNoUser")) - 1;

            if (responderIndex >= 0 && responderIndex < players.size()
                    && lastUserIndex >= 0 && lastUserIndex < players.size()) {
                request.startJustSayNoResponse(players.get(lastUserIndex), players.get(responderIndex));
            }
        }

        return request;
    }

    // Parses int.
    private static int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    public static final class Snapshot {
        public final ArrayList<Player> players = new ArrayList<>();
        public int you;
        public int currentPlayerIndex;
        public int turnRemainingSeconds = -1;
        public boolean discard;
        public boolean win;
        public Game.PaymentRequest paymentRequest;
    }

    private record PlayerSummary(int handCount, int usedCount, String name) {
        private static final PlayerSummary EMPTY = new PlayerSummary(0, 0, null);
    }
}
