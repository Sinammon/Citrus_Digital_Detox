import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

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
                passCard("YouTube", "Pause your YouTube block for 10 minutes.", "youtube"),
                passCard("Netflix", "Pause your Netflix block for 10 minutes.", "netflix"),
                passCard("Spotify", "Pause your Spotify block for 10 minutes.", "spotify"),
                passCard("WhatsApp", "Pause your WhatsApp block for 10 minutes.", "whatsapp"),
                passCard("Instagram", "Pause your Instagram block for 10 minutes.", "instagram")
        );

        root.getChildren().addAll(intro, passes);
        return root;
    }

    private VBox passCard(String service, String detailText, String target) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "accent-card", "pass-card");
        card.setPrefWidth(230);

        Label title = new Label(service + " pass — 10 minutes");
        title.getStyleClass().add("section-title");
        Label detail = new Label(detailText + " Costs 5 coins.");
        detail.getStyleClass().add("muted");
        detail.setWrapText(true);

        Button buy = new Button("Buy for 5 coins");
        buy.getStyleClass().add("primary-button");
        buy.setOnAction(event -> {
            if (economy.spendCoins(5)) {
                blockManager.addPass(new Pass(target, 10));
                message("Pass purchased", service + " is unlocked for 10 minutes.");
            } else {
                message("Not enough coins", "Keep focusing to earn more coins.");
            }
        });

        card.getChildren().addAll(title, detail, buy);
        return card;
    }

    private void message(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
