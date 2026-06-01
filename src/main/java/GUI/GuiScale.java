package GUI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import logic.Game;

/**
 * Maps between design coordinates (fixed layout size) and canvas display coordinates.
 *
 * <p>Instance API: create/update per canvas. Static helpers delegate to a default
 * instance so existing callers ({@code GameListener}, {@code GameScreen}, etc.)
 * keep working without changes.</p>
 */
public class GuiScale {

    public static final double DISPLAY_SCALE = 1.2;

    private static final GuiScale DEFAULT =
            new GuiScale(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);

    static {
        DEFAULT.update(
                Game.SCREEN_WIDTH * DISPLAY_SCALE,
                Game.SCREEN_HEIGHT * DISPLAY_SCALE
        );
    }

    private final double designWidth;
    private final double designHeight;
    private double scaleX = 1.0;
    private double scaleY = 1.0;

    // Creates a GuiScale instance.
    public GuiScale(double designWidth, double designHeight) {
        this.designWidth = designWidth;
        this.designHeight = designHeight;
    }

    // Checks whether this can vas width.
    public static double canvasWidth() {
        return DEFAULT.s(DEFAULT.designWidth);
    }

    // Checks whether this can vas height.
    public static double canvasHeight() {
        return DEFAULT.s(DEFAULT.designHeight);
    }

    // Prepares this operation.
    public static void prepare(GraphicsContext gc) {
        gc.clearRect(0, 0, canvasWidth(), canvasHeight());
        Affine transform = new Affine();
        transform.appendScale(DEFAULT.scaleX, DEFAULT.scaleY);
        gc.setTransform(transform);
    }

    // Clears the current state.
    public static void clear(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTransform(new Affine());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // Runs to logical.
    public static double toLogical(double displayCoordinate) {
        return DEFAULT.toLogicalX(displayCoordinate);
    }

    // Runs update.
    public void update(double displayWidth, double displayHeight) {
        if (displayWidth <= 0 || displayHeight <= 0) {
            scaleX = 1.0;
            scaleY = 1.0;
            return;
        }
        scaleX = displayWidth / designWidth;
        scaleY = displayHeight / designHeight;
    }

    // Runs s.
    public double s(double designValue) {
        return designValue * scaleX;
    }

    // Runs to logical x.
    public double toLogicalX(double displayX) {
        return scaleX == 0 ? displayX : displayX / scaleX;
    }
}
