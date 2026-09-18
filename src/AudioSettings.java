import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioSettings {
    private MediaPlayer player;

    public Node getView() {
        VBox root = new VBox(12);
        root.getStyleClass().add("audio-control");
        HBox heading = new HBox(10, createAudioIcon(), new Label("Chill background music"));
        heading.getChildren().get(1).getStyleClass().add("section-title");
        Slider volume = new Slider(0, 1, 0.35);
        volume.setMaxWidth(Double.MAX_VALUE);
        volume.getStyleClass().add("audio-slider");
        HBox.setHgrow(volume, Priority.ALWAYS);
        Label value = new Label("35%");
        value.getStyleClass().add("slider-value");
        volume.valueProperty().addListener((obs, oldValue, newValue) -> value.setText(Math.round(newValue.doubleValue() * 100) + "%"));
        HBox row = new HBox(12, new Label("Volume"), volume, value);
        HBox.setHgrow(volume, Priority.ALWAYS);
        root.getChildren().addAll(heading, row);
        startPlayback(volume);
        return root;
    }

    private void startPlayback(Slider volume) {
        var resource = getClass().getResource("/audio/chill-vibes.mp3");
        if (resource == null) return;
        try {
            player = new MediaPlayer(new Media(resource.toExternalForm()));
            player.setCycleCount(MediaPlayer.INDEFINITE);
            player.volumeProperty().bind(volume.valueProperty());
            player.play();
        } catch (RuntimeException ignored) {
            // The settings control remains available on systems without an MP3 codec.
        }
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
