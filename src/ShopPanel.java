import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import java.util.Locale;

public class ShopPanel {
    private final BlockManager blockManager;
    private final UserEconomy economy;
    private final FlowPane passes = new FlowPane(16, 16);

    public ShopPanel(BlockManager blockManager, UserEconomy economy) {
        this.blockManager = blockManager;
        this.economy = economy;
    }

    public Node getView() {
        VBox root = new VBox(18);
        root.getStyleClass().add("content");
        Label intro = new Label("Spend your earned coins on a planned break.");
        intro.getStyleClass().add("muted");
        passes.setPadding(new Insets(4, 0, 0, 0));
        passes.setPrefWrapLength(720);
        passes.getChildren().setAll(
                passCard("Youtube", "Pause your Youtube block for 10 minutes.", "youtube"),
                passCard("Netflix", "Pause your Netflix block for 10 minutes.", "netflix"),
                passCard("Spotify", "Pause your Spotify block for 10 minutes.", "spotify"),
                passCard("WhatsApp", "Pause your WhatsApp block for 10 minutes.", "whatsapp"),
                passCard("Instagram", "Pause your Instagram block for 10 minutes.", "instagram"),
                passCard("Discord", "Pause your Discord block for 10 minutes.", "discord")
        );
        Button custom = new Button("+ Create custom pass");
        custom.getStyleClass().add("secondary-button");
        custom.setOnAction(event -> showCustomPassForm());
        root.getChildren().addAll(intro, passes, custom);
        return root;
    }

    private VBox passCard(String service, String detailText, String target) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "pass-card");
        card.setPrefWidth(230);
        HBox heading = new HBox(10, createAppIcon(service), new Label(service + " Pass"));
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getChildren().get(1).getStyleClass().add("section-title");
        Label detail = new Label(detailText);
        detail.getStyleClass().add("muted");
        detail.setWrapText(true);
        Button buy = new Button("Buy for 5 coins");
        buy.getStyleClass().add("primary-button");
        buy.setOnAction(event -> purchase(service, target));
        card.getChildren().addAll(heading, detail, buy);
        return card;
    }

    private void showCustomPassForm() {
        VBox form = new VBox(12);
        TextField name = new TextField(); name.setPromptText("Pass title");
        TextField description = new TextField(); description.setPromptText("Short description");
        Button create = new Button("Create pass"); create.getStyleClass().add("primary-button");
        form.getChildren().addAll(new Label("Create your own reward pass"), name, description, create);
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Custom reward pass");
        stage.setScene(new javafx.scene.Scene(form, 380, 230));
        create.setOnAction(event -> {
            if (name.getText().isBlank() || description.getText().isBlank()) return;
            String target = name.getText().trim().toLowerCase(Locale.ROOT);
            passes.getChildren().add(passCard(name.getText().trim(), description.getText().trim(), target));
            stage.close();
        });
        stage.showAndWait();
    }

    private void purchase(String service, String target) {
        if (economy.spendCoins(5)) {
            blockManager.addPass(new Pass(target, 10));
            message("Pass purchased", service + " is unlocked for 10 minutes.");
        } else message("Not enough coins", "Keep focusing to earn more coins.");
    }

    private Node createAppIcon(String app) {
        Color color = switch (app) {
            case "Youtube" -> Color.web("#FF3D3D");
            case "Netflix" -> Color.web("#D81F32");
            case "Spotify" -> Color.web("#1DB954");
            case "WhatsApp" -> Color.web("#25D366");
            case "Instagram" -> Color.web("#C13584");
            case "Discord" -> Color.web("#5865F2");
            default -> Color.WHITE;
        };
        Circle background = new Circle(18, color);
        Circle inner = new Circle(11, Color.TRANSPARENT);
        inner.setStroke(Color.WHITE); inner.setStrokeWidth(2);
        Polygon mark = new Polygon(-5, -3, 0, -7, 6, -3, 4, 5, 0, 8, -4, 5);
        mark.setFill(Color.WHITE);
        StackPane icon = new StackPane(new Group(background, inner, mark));
        icon.setPrefSize(36, 36); icon.setMinSize(36, 36); icon.setMaxSize(36, 36);
        return icon;
    }

    private void message(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }
}
