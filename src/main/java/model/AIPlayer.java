package model;

public class AIPlayer extends Player{
    private final String difficulty;

    public AIPlayer(DrawPileAndDiscardPile drawCardsAndDiscardPile,String difficulty) {
        super(drawCardsAndDiscardPile);
        this.difficulty=difficulty;
    }
}
