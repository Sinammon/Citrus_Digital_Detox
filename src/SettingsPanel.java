import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsPanel {
    private final UserEconomy economy;
    private final AudioSettings audioSettings;
    public SettingsPanel(UserEconomy economy, AudioSettings audioSettings) { this.economy = economy; this.audioSettings = audioSettings; }

    public Node getView() {
        VBox root = new VBox(20); root.getStyleClass().add("content");
        VBox goalCard = new VBox(10); goalCard.getStyleClass().add("card");
        Label goalTitle = new Label("Daily Goal"); goalTitle.getStyleClass().add("section-title");
        Label goalDescription = new Label("Choose how many focused hours you want to complete each day (1–10 hours)."); goalDescription.getStyleClass().add("muted");
        Slider goal = new Slider(1, 10, economy.getDailyGoalHours());
        goal.setBlockIncrement(1); goal.setMajorTickUnit(1); goal.setMinorTickCount(0); goal.setSnapToTicks(true); goal.setShowTickMarks(true); goal.setShowTickLabels(true); goal.setMaxWidth(Double.MAX_VALUE);
        Label goalValue = new Label(); goalValue.getStyleClass().add("slider-value");
        Runnable updateGoal = () -> goalValue.setText(Math.round(goal.getValue()) + " hour" + (Math.round(goal.getValue()) == 1 ? "" : "s"));
        goal.valueProperty().addListener((obs, oldValue, newValue) -> updateGoal.run()); updateGoal.run();
        HBox goalSliderRow = new HBox(14, goal, goalValue); goalSliderRow.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(goal, Priority.ALWAYS);
        Button saveGoal = new Button("Save daily goal"); saveGoal.getStyleClass().add("primary-button");
        saveGoal.setOnAction(event -> economy.setDailyGoalHours(Math.round(goal.getValue())));
        goalCard.getChildren().addAll(goalTitle, goalDescription, goalSliderRow, saveGoal);

        VBox coinCard = new VBox(10); coinCard.getStyleClass().add("card");
        Label title = new Label("Coin rate"); title.getStyleClass().add("section-title");
        Label description = new Label("How many focused seconds earn one coin?"); description.getStyleClass().add("muted");
        Slider rate = new Slider(60, 3600, economy.getSecondsPerCoin());
        rate.setBlockIncrement(30); rate.setMajorTickUnit(30); rate.setSnapToTicks(true); rate.setMaxWidth(Double.MAX_VALUE);
        Label value = new Label(); value.getStyleClass().add("slider-value");
        Runnable update = () -> value.setText(Math.round(rate.getValue()) + " seconds");
        rate.valueProperty().addListener((obs, oldValue, newValue) -> update.run()); update.run();
        HBox sliderRow = new HBox(14, rate, value); sliderRow.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(rate, Priority.ALWAYS);
        Button save = new Button("Save coin rate"); save.getStyleClass().add("primary-button"); save.setOnAction(event -> economy.setSecondsPerCoin((int) Math.round(rate.getValue())));
        coinCard.getChildren().addAll(title, description, sliderRow, save);
        VBox tutorial = new VBox(8); tutorial.getStyleClass().add("card"); Label tutorialTitle = new Label("How to use Citrus"); tutorialTitle.getStyleClass().add("section-title"); tutorial.getChildren().addAll(tutorialTitle, new TutorialContent().getView());
        VBox music = new VBox(8); music.getStyleClass().add("card"); music.getChildren().add(audioSettings.getView());
        root.getChildren().addAll(goalCard, coinCard, tutorial, music); return root;
    }
}
