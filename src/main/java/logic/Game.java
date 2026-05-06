package logic;

import java.util.ArrayList;
import java.util.Scanner;

import model.ActionCards;
import model.Card;
import model.DrawPileAndDiscardPile;
import model.MoneyCards;
import model.Player;
import model.PropertiesCards;

public class Game {
    private ArrayList<Player> players;
    private DrawPileAndDiscardPile drawCards;
    private boolean isWin;

    public static double SCREEN_WIDTH = 1035;
    public static double SCREEN_HEIGHT = 625;

    private int currentPlayerIndex;
    private boolean isDiscard = false;

    public Game() {
        players = new ArrayList<>();
        drawCards = new DrawPileAndDiscardPile();
        isWin = false;
        currentPlayerIndex = 0;
        addPlayer();
    }

    // ==================== 核心主流程 ====================

    public void startGame() {
        players.clear();
        drawCards = new DrawPileAndDiscardPile();
        currentPlayerIndex = 0;
        isWin = false;
        isDiscard = false;
        addPlayer();
        Player currentPlayer = getCurrentPlayer();
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);
        System.out.println("Monopoly Deal GUI game started.");
    }

    /**
     * CLI 演示主循环
     * 先保证最小可玩闭环：
     * 初始化 -> 摸牌 -> 最多3次出牌 -> 超7弃牌 -> 胜利判断 -> 回合切换
     */
    public void mainLoop() {
        Scanner scanner = new Scanner(System.in);

        while (!isWin) {
            Player currentPlayer = getCurrentPlayer();
            startTurn(currentPlayer);

            while (currentPlayer.getUseCardTimes() < 3 && !isWin) {
                System.out.println("\n=================================");
                System.out.println("当前玩家：玩家 " + (currentPlayerIndex + 1));
                System.out.println("已出牌次数：" + currentPlayer.getUseCardTimes() + "/3");
                System.out.println("手牌如下：");
                currentPlayer.printAllCardsOfHands();
                System.out.println("输入手牌序号出牌（从1开始），输入 -1 结束出牌阶段：");

                int choice;
                try {
                    choice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("请输入数字！");
                    scanner.nextLine();
                    continue;
                }

                if (choice == -1) {
                    break;
                }

                if (choice < 1 || choice > currentPlayer.getHandCards().size()) {
                    System.out.println("请输入有效的卡牌序号！");
                    continue;
                }

                Card selectedCard = currentPlayer.getHandCards().get(choice - 1);
                playCard(selectedCard);

                if (currentPlayer.checkIfWin()) {
                    isWin = true;
                    break;
                }
            }

            if (isWin) {
                System.out.println("🏆 玩家 " + (currentPlayerIndex + 1) + " 获胜！");
                break;
            }

            startDiscard();

            while (isDiscard) {
                System.out.println("\n玩家 " + (currentPlayerIndex + 1) + " 需要弃牌。");
                currentPlayer.printAllCardsOfHands();
                System.out.println("请输入要弃掉的手牌序号（从1开始）：");

                int discardChoice;
                try {
                    discardChoice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("请输入数字！");
                    scanner.nextLine();
                    continue;
                }

                if (discardChoice < 1 || discardChoice > currentPlayer.getHandCards().size()) {
                    System.out.println("请输入有效的弃牌序号！");
                    continue;
                }

                Card discardCard = currentPlayer.getHandCards().get(discardChoice - 1);
                discard(discardCard);
            }

            endTurn(currentPlayer);
        }
    }

    /**
     * 回合开始：
     * - 若当前玩家手牌为空，摸5张
     * - 否则摸2张
     * - 重置本回合出牌次数
     */
    public void startTurn(Player currentPlayer) {
        currentPlayer.setOnTurn(true);
        currentPlayer.setUseCardTimes(0);

        int drawNum = currentPlayer.getHandCards().isEmpty() ? 5 : 2;
        currentPlayer.takeCard(drawNum);

        System.out.println("\n➡️ 玩家 " + (currentPlayerIndex + 1) + " 的回合开始！");
        System.out.println("摸了 " + drawNum + " 张牌，当前手牌数：" + currentPlayer.getHandCards().size());
    }

    /**
     * 回合结束：
     * - 若已获胜，结束游戏
     * - 若手牌超过7张，不能结束，必须先弃牌
     * - 否则切换到下一位玩家
     */
    public void endTurn(Player currentPlayer) {
        if (currentPlayer.checkIfWin()) {
            isWin = true;
            return;
        }

        if (currentPlayer.getHandCards().size() > 7) {
            isDiscard = true;
            System.out.println("⚠️ 仍有超过7张手牌，不能结束回合！");
            return;
        }

        currentPlayer.setOnTurn(false);
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        System.out.println("➡️ 回合结束，切换到下一位玩家。");
    }

    public void guiEndTurn() {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer.checkIfWin()) {
            isWin = true;
            return;
        }

        if (currentPlayer.getHandCards().size() > 7) {
            isDiscard = true;
            return;
        }

        currentPlayer.setOnTurn(false);
        currentPlayer.setUseCardTimes(0);

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        Player nextPlayer = getCurrentPlayer();
        startTurn(nextPlayer);
    }

    /**
     * 若手牌超过7张，进入弃牌阶段
     */
    public void startDiscard() {
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer.getHandCards().size() > 7) {
            isDiscard = true;
            System.out.println("⚠️ 玩家 " + (currentPlayerIndex + 1) + " 手牌超过7张，进入弃牌阶段！");
        }
    }

    /**
     * 弃牌
     */
    public void discard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!isDiscard) {
            System.out.println("当前不在弃牌阶段，不能弃牌！");
            return;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("该卡不在当前玩家手牌中，无法弃掉！");
            return;
        }

        currentPlayer.getHandCards().remove(card);
        drawCards.getDiscardPile().add(card);

        System.out.println("弃牌成功，当前手牌数：" + currentPlayer.getHandCards().size());

        if (currentPlayer.getHandCards().size() <= 7) {
            isDiscard = false;
            System.out.println("✅ 弃牌阶段结束。");
        }
    }

    /**
     * 出牌逻辑：
     * - 仅当前玩家自己的回合可出牌
     * - 每回合最多3次
     * - 必须是当前玩家手中的牌
     */
    public void playCard(Card card) {
        Player currentPlayer = getCurrentPlayer();

        if (!currentPlayer.isOnTurn()) {
            System.out.println("现在不是当前玩家的回合，不能出牌！");
            return;
        }

        if (currentPlayer.getUseCardTimes() >= 3) {
            System.out.println("本回合已达到3次出牌上限！");
            return;
        }

        if (!currentPlayer.getHandCards().contains(card)) {
            System.out.println("该卡不在当前玩家手牌中，无法出牌！");
            return;
        }

        boolean success = false;

        if (card instanceof MoneyCards) {
            currentPlayer.putMoneyCard(card);
            System.out.println("💰 打出了一张金钱牌。");
            success = true;
        } else if (card instanceof PropertiesCards) {
            currentPlayer.putPropertyCard((PropertiesCards) card);
            System.out.println("🏠 打出了一张地产牌。");
            success = true;
        } else if (card instanceof ActionCards) {
            currentPlayer.putActionCard((ActionCards) card);
            System.out.println("⚡ 打出了一张行动牌。");
            success = true;
        } else {
            System.out.println("未知卡牌类型，无法出牌。");
        }

        if (success) {
            currentPlayer.setUseCardTimes(currentPlayer.getUseCardTimes() + 1);
            System.out.println("当前已出牌次数：" + currentPlayer.getUseCardTimes() + "/3");
        }

        if (currentPlayer.checkIfWin()) {
            isWin = true;
        }
    }

    // ==================== 初始化与辅助方法 ====================

    /**
     * 初始化4名玩家，每人初始5张手牌
     */
    private void addPlayer() {
        for (int i = 0; i < 4; i++) {
            Player p = new Player(drawCards);
            players.add(p);
        }

        // 添加敌人列表
        for (int i = 0; i < players.size(); i++) {
            for (int j = 0; j < players.size(); j++) {
                if (i != j) {
                    players.get(i).getEnemy().add(players.get(j));
                }
            }
        }

        // 初始发牌：每人5张
        for (Player p : players) {
            p.takeCard(5);
        }

        System.out.println("✅ 游戏初始化完成：4名玩家，每人初始5张手牌。");
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public DrawPileAndDiscardPile getDrawCards() {
        return drawCards;
    }

    public void setDrawCards(DrawPileAndDiscardPile drawCards) {
        this.drawCards = drawCards;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public boolean isDiscard() {
        return isDiscard;
    }

    public static double getScreenWidth() {
        return SCREEN_WIDTH;
    }

    public static void setScreenWidth(double screenWidth) {
        SCREEN_WIDTH = screenWidth;
    }

    public static double getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    public static void setScreenHeight(double screenHeight) {
        SCREEN_HEIGHT = screenHeight;
    }
}