import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsPanel {
    private final UserEconomy economy;
    public SettingsPanel(UserEconomy economy) { this.economy = economy; }

    public Node getView() {
        VBox root = new VBox(20); root.getStyleClass().add("content");
        VBox coinCard = new VBox(10); coinCard.getStyleClass().add("card");
        Label title = new Label("Coin rate"); title.getStyleClass().add("section-title");
        Label description = new Label("How many focused seconds earn one coin?"); description.getStyleClass().add("muted");
        Slider rate = new Slider(60, 3600, economy.getSecondsPerCoin());
        rate.setBlockIncrement(30); rate.setMajorTickUnit(30); rate.setSnapToTicks(true); rate.setMaxWidth(Double.MAX_VALUE);
        Label value = new Label(); value.getStyleClass().add("slider-value");
        Runnable update = () -> value.setText(Math.round(rate.getValue()) + " seconds");
        rate.valueProperty().addListener((obs, oldValue, newValue) -> update.run()); update.run();
        HBox sliderRow = new HBox(14, rate, value); sliderRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(rate, javafx.scene.layout.Priority.ALWAYS);
        Button save = new Button("Save coin rate"); save.getStyleClass().add("primary-button");
        save.setOnAction(event -> economy.setSecondsPerCoin((int) Math.round(rate.getValue())));
        coinCard.getChildren().addAll(title, description, sliderRow, save);
        VBox tutorial = new VBox(8); tutorial.getStyleClass().add("card"); Label tutorialTitle = new Label("How to use Citrus"); tutorialTitle.getStyleClass().add("section-title"); tutorial.getChildren().addAll(tutorialTitle, new TutorialContent().getView());
        VBox music = new VBox(8); music.getStyleClass().add("card"); Label musicTitle = new Label("Chill background music"); musicTitle.getStyleClass().add("section-title"); Label coming = new Label("Coming soon — a calm focus soundtrack player will live here."); coming.getStyleClass().add("muted"); music.getChildren().addAll(musicTitle, coming);
        root.getChildren().addAll(coinCard, tutorial, music); return root;
    }
}
