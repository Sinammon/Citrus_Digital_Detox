import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class AudioSettings {
    private MediaPlayer player;
    private double volume = 0.35;
    private boolean started;

    public Node getView() {
        VBox root = new VBox(12);
        root.getStyleClass().add("audio-control");
        HBox heading = new HBox(10, createAudioIcon(), new Label("Chill background music"));
        heading.getChildren().get(1).getStyleClass().add("section-title");

        Slider volumeSlider = new Slider(0, 1, volume);
        volumeSlider.setMaxWidth(Double.MAX_VALUE);
        volumeSlider.getStyleClass().add("audio-slider");
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        Label value = new Label(Math.round(volume * 100) + "%");
        value.getStyleClass().add("slider-value");
        Button start = new Button(started ? "Music playing" : "Start music");
        start.getStyleClass().add("primary-button");
        start.setDisable(started);
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> setVolume(newValue.doubleValue(), value, start));
        start.setOnAction(event -> {
            ensurePlayer();
            if (player != null && volume > 0) {
                started = true;
                player.play();
                start.setText("Music playing");
                start.setDisable(true);
            }
        });

        HBox row = new HBox(12, new Label("Volume"), volumeSlider, value, start);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        root.getChildren().addAll(heading, row);
        ensurePlayer();
        return root;
    }

    private void setVolume(double nextVolume, Label value, Button start) {
        volume = nextVolume;
        value.setText(Math.round(volume * 100) + "%");
        ensurePlayer();
        if (player == null) return;
        player.setVolume(volume);
        if (volume <= 0) {
            player.pause();
            started = false;
            start.setText("Start music");
            start.setDisable(false);
        } else if (started) {
            player.play();
        }
    }

    private void ensurePlayer() {
        if (player != null) return;
        var resource = getClass().getResource("/audio/chill-vibes.mp3");
        if (resource == null) return;
        try {
            player = new MediaPlayer(new Media(resource.toExternalForm()));
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.setVolume(volume);
        } catch (RuntimeException ignored) {
            player = null;
        }
    }

    public void stop() {
        if (player != null) player.stop();
        started = false;
    }

    private Node createAudioIcon() {
        Circle disc = new Circle(18, Color.web("#FDCC21"));
        Circle center = new Circle(5, Color.web("#FFFDF4"));
        Arc groove = new Arc(0, 0, 11, 11, 35, 220); groove.setType(ArcType.OPEN); groove.setFill(Color.TRANSPARENT); groove.setStroke(Color.web("#B98200")); groove.setStrokeWidth(2);
        Line stem = new Line(8, -9, 8, 5); stem.setStroke(Color.web("#B98200")); stem.setStrokeWidth(2);
        Polygon flag = new Polygon(8, -9, 15, -6, 8, -3); flag.setFill(Color.web("#B98200"));
        StackPane icon = new StackPane(disc, groove, center, stem, flag);
        icon.setPrefSize(36, 36); icon.setMinSize(36, 36); icon.setMaxSize(36, 36);
        return icon;
    }
}
