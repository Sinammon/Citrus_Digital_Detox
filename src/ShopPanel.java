import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class ShopPanel {
    private final BlockManager blockManager;
    private final UserEconomy economy;

    public ShopPanel(BlockManager blockManager, UserEconomy economy) {
        this.blockManager = blockManager;
        this.economy = economy;
    }

    public Node getView() {
        VBox root = new VBox(18);
        root.getStyleClass().add("content");
        Label intro = new Label("Spend your earned coins on a planned break.");
        intro.getStyleClass().add("muted");

        FlowPane passes = new FlowPane(16, 16);
        passes.setPadding(new Insets(4, 0, 0, 0));
        passes.setPrefWrapLength(720);
        passes.getChildren().addAll(
                passCard("Youtube", "Pause your Youtube block for 10 minutes.", "youtube"),
                passCard("Netflix", "Pause your Netflix block for 10 minutes.", "netflix"),
                passCard("Spotify", "Pause your Spotify block for 10 minutes.", "spotify"),
                passCard("WhatsApp", "Pause your WhatsApp block for 10 minutes.", "whatsapp"),
                passCard("Instagram", "Pause your Instagram block for 10 minutes.", "instagram"),
                passCard("Discord", "Pause your Discord block for 10 minutes.", "discord")
        );
        root.getChildren().addAll(intro, passes);
        return root;
    }

    private VBox passCard(String service, String detailText, String target) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "accent-card", "pass-card");
        card.setPrefWidth(230);
        HBox heading = new HBox(10, createAppIcon(service), new Label(service + " Pass"));
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getChildren().get(1).getStyleClass().add("section-title");
        Label detail = new Label(detailText + " Costs 5 coins.");
        detail.getStyleClass().add("muted");
        detail.setWrapText(true);
        Button buy = new Button("Buy for 5 coins");
        buy.getStyleClass().add("primary-button");
        buy.setOnAction(event -> {
            if (economy.spendCoins(5)) {
                blockManager.addPass(new Pass(target, 10));
                message("Pass purchased", service + " is unlocked for 10 minutes.");
            } else message("Not enough coins", "Keep focusing to earn more coins.");
        });
        card.getChildren().addAll(heading, detail, buy);
        return card;
    }

    private Node createAppIcon(String app) {
        Color color = switch (app) {
            case "Youtube" -> Color.web("#FF3D3D");
            case "Netflix" -> Color.web("#D81F32");
            case "Spotify" -> Color.web("#1DB954");
            case "WhatsApp" -> Color.web("#25D366");
            case "Instagram" -> Color.web("#C13584");
            default -> Color.web("#5865F2");
        };
        Circle background = new Circle(18, color);
        Circle inner = new Circle(11, Color.TRANSPARENT);
        inner.setStroke(Color.WHITE);
        inner.setStrokeWidth(2);
        Polygon mark = new Polygon(-5, -3, 0, -7, 6, -3, 4, 5, 0, 8, -4, 5);
        mark.setFill(Color.WHITE);
        StackPane icon = new StackPane(new Group(background, inner, mark));
        icon.setPrefSize(36, 36);
        icon.setMinSize(36, 36);
        icon.setMaxSize(36, 36);
        return icon;
    }

    private void message(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
