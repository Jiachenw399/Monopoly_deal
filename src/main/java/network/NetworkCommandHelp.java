package network;

final class NetworkCommandHelp {
    static final String TEXT = """
            Commands (type HELP anytime):
              HELLO, PLAYERS, START_GAME, STATE
              END_TURN - end your turn (current player only)
              PLAY_CARD <n>, DISCARD <n> - hand cards are numbered from 1
              PLAY_AS_MONEY <n> - bank an action card as money
              PAY B1 HOUSE:BROWN HOTEL:BROWN P2 - pay during payment phase (B=bank, HOUSE/HOTEL=building, P=property)
              JUST_SAY_NO
              PASS_GO, BIRTHDAY, DEBT, SLY, DEAL_BREAKER, RENT, RENT_ANY, HOUSE, HOTEL, FORCED_DEAL
              QUIT - leave the game
            """;

    // Creates a NetworkCommandHelp instance.
    private NetworkCommandHelp() {
    }
}
