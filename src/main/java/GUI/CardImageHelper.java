package GUI;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import model.ActionCardType;
import model.ActionCards;
import model.Card;
import model.HiddenCard;
import model.MoneyCards;
import model.PropertiesCards;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CardImageHelper {
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    // Creates a CardImageHelper instance.
    private CardImageHelper() {
    }

    // Draws card image.
    public static boolean drawCardImage(GraphicsContext gc,
                                        Card card,
                                        double x,
                                        double y,
                                        double width,
                                        double height) {
        Image image = getCardImage(card);

        if (image == null || image.isError()) {
            return false;
        }

        gc.drawImage(image, x, y, width, height);
        return true;
    }

    // Finds card image.
    private static Image getCardImage(Card card) {
        String path = getCardImagePath(card);

        if (path == null) {
            return null;
        }

        return loadImage(path);
    }

    // Finds card image path.
    private static String getCardImagePath(Card card) {
        if (card instanceof HiddenCard) {
            return "/images/card_back.jpg";
        }

        if (card instanceof MoneyCards) {
            return "/images/money/money_" + card.getValue() + ".png";
        }

        if (card instanceof PropertiesCards propertyCard) {
            if (propertyCard.isWildCard()) {
                return "/images/property_wildcards/" + propertyCard.getImageFileName();
            }

            return "/images/property/" + propertyCard.getImageFileName();
        }

        if (card instanceof ActionCards actionCard) {
            ActionCardType type = actionCard.getActionCardType();
            String fileName = type.name().toLowerCase() + ".png";

            if (isRentCard(type)) {
                return "/images/rent/" + fileName;
            }

            return "/images/action/" + fileName;
        }

        return null;
    }

    // Checks whether rent card.
    private static boolean isRentCard(ActionCardType type) {
        return type.name().startsWith("RENT_WITH");
    }

    // Runs load image.
    private static Image loadImage(String path) {
        Image cached = IMAGE_CACHE.get(path);
        if (cached != null || IMAGE_CACHE.containsKey(path)) {
            return cached;
        }

        InputStream inputStream = CardImageHelper.class.getResourceAsStream(path);

        if (inputStream == null) {
            System.out.println("[CARD IMAGE] Missing image: " + path);
            return null;
        }

        Image image = new Image(inputStream);

        if (image.isError()) {
            System.out.println("[CARD IMAGE] Image load error: " + path);
            return null;
        }

        IMAGE_CACHE.put(path, image);
        return image;
    }

    // Draws hand number badge.
    public static void drawHandNumberBadge(GraphicsContext gc,
                                           int number,
                                           double x,
                                           double y) {
        gc.setFill(Color.rgb(255, 255, 255, 0.9));
        gc.fillOval(x, y, 22, 22);

        gc.setStroke(Color.rgb(30, 35, 48));
        gc.strokeOval(x, y, 22, 22);

        gc.setFill(Color.rgb(30, 35, 48));
        gc.setFont(Font.font("Arial", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(number), x + 11, y + 11);

        gc.setTextBaseline(VPos.TOP);
    }
}
