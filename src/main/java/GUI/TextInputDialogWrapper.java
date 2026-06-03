package GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextInputDialogWrapper {
    private final List<TextField> fields = new ArrayList<>();
    private final Dialog<List<String>> dialog;

    public TextInputDialogWrapper(Window owner, int fieldCount) {
        dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Enter Player Names");
        dialog.setHeaderText("Enter a name for each player, then click OK to start.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10, 20, 10, 20));

        for (int i = 0; i < fieldCount; i++) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label label = new Label("Player " + (i + 1) + ":");
            label.setPrefWidth(70);

            TextField field = new TextField();
            field.setPrefWidth(220);
            field.setPromptText("Enter name...");
            fields.add(field);

            row.getChildren().addAll(label, field);
            content.getChildren().add(row);
        }

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
                String name = fields.get(i).getText();
                if (name == null || name.trim().isEmpty()) {
                    name = "Player " + (i + 1);
                }
                names.add(name.trim());
            }
            return names;
        });
    }

    public Optional<List<String>> showAndWait() {
        return dialog.showAndWait();
    }
}
