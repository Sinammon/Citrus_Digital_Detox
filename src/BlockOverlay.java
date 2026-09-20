import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class BlockOverlay {
    private final Block block;
    private final BlockManager blockManager;
    private final Stage stage = new Stage();
    private Timeline delayCountdown;

    public BlockOverlay(Block block, BlockManager blockManager) {
        this.block = block; this.blockManager = blockManager;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(block.getTargetName());
        stage.setAlwaysOnTop(true);
        VBox root = new VBox(16); root.setAlignment(Pos.CENTER); root.getStyleClass().add("overlay");
        Label heading = new Label("🍋 " + block.getTargetName() + " is blocked"); heading.getStyleClass().add("overlay-title");
        Label detail = new Label("Take a breath. Citrus is protecting your focus."); detail.getStyleClass().add("overlay-detail");
        root.getChildren().addAll(heading, detail); addUnlockControls(root);
        Scene scene = new Scene(root); scene.getStylesheets().add(getClass().getResource("/citrus.css").toExternalForm());
        stage.setScene(scene); stage.setMaximized(true); stage.show();
    }
    private void addUnlockControls(VBox root) {
        if (block.getLockType() == LockType.EMERGENCY) {
            PasswordField field = new PasswordField(); field.setPromptText("Emergency password"); field.setMaxWidth(280);
            Button unlock = new Button("Unlock for 10 minutes"); unlock.getStyleClass().add("primary-button"); unlock.setOnAction(e -> { if (field.getText().equals(block.getEmergencyPassword())) unlock(); });
            root.getChildren().addAll(field, unlock);
        } else if (block.getLockType() == LockType.RANDOM_TEXT) {
            Label instruction = new Label("Type " + block.getChallengeLength() + " different words (2+ letters each).");
            TextField field = new TextField(); field.setMaxWidth(420); field.setPromptText("Write your words here");
            Label error = new Label(); error.getStyleClass().add("overlay-error");
            Button submit = new Button("Continue"); submit.getStyleClass().add("primary-button"); submit.setOnAction(e -> { if (block.validateRandomTextInput(field.getText())) unlock(); else error.setText("Try again — check the word count and duplicates."); });
            root.getChildren().addAll(instruction, field, submit, error);
        } else if (block.getLockType() == LockType.PASS_BLOCK) {
            Label shopOnly = new Label("This block can only be unlocked by purchasing a pass from the Shop.");
            shopOnly.getStyleClass().add("overlay-detail");
            root.getChildren().add(shopOnly);
        } else if (block.getLockType() == LockType.DELAY) {
            Label countdown = new Label(); countdown.getStyleClass().add("overlay-title");
            int delaySeconds = Math.max(0, block.getDelaySeconds());
            countdown.setText("Unlocking in " + delaySeconds + " seconds");
            root.getChildren().add(countdown);
            int[] remaining = {delaySeconds};
            delayCountdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                if (remaining[0] <= 1) { remaining[0] = 0; countdown.setText("Unlocking in 0 seconds"); delayCountdown.stop(); unlock(); }
                else { remaining[0]--; countdown.setText("Unlocking in " + remaining[0] + " seconds"); }
            }));
            delayCountdown.setCycleCount(Math.max(1, delaySeconds));
            if (delaySeconds == 0) unlock(); else delayCountdown.play();
        }
    }
    private void unlock() { if (delayCountdown != null) delayCountdown.stop(); blockManager.addPass(new Pass(block.getTargetName(), 10)); stage.close(); }
    public void dispose() { if (delayCountdown != null) delayCountdown.stop(); stage.close(); }
}
