package network;

final class NetworkCommandHelp {
    static final String TEXT = """
            Commands (type HELP anytime):
              HELLO, PLAYERS, START_GAME, STATE
              END_TURN — end your turn (current player only)
              PLAY_CARD <n>, DISCARD <n> — hand cards are numbered from 1
              PAY B1 P2 — pay during payment phase (B=bank, P=property)
              JUST_SAY_NO
              BIRTHDAY, DEBT, SLY, DEAL_BREAKER, RENT, RENT_ANY, HOUSE, HOTEL
              QUIT — leave the game
            """;

    private NetworkCommandHelp() {
    }
}