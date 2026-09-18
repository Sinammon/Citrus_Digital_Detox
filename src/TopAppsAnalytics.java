import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class TopAppsAnalytics {
    private final UserEconomy economy;
    private final List<String> appNames = List.of("Youtube", "Instagram", "Spotify", "Netflix");
    private final VBox rows = new VBox(10);

    public TopAppsAnalytics(UserEconomy economy) { this.economy = economy; }

    public Node getView() {
        VBox root = new VBox(12);
        root.getStyleClass().add("card");
        Label title = new Label("TOP APPS & WEBSITES");
        title.getStyleClass().add("analytics-title");
        root.getChildren().addAll(title, rows);
        refresh();
        return root;
    }

    public void refresh() {
        rows.getChildren().clear();
        int minutes = Math.max(1, (int) economy.getTotalProductiveMinutes());
        for (int i = 0; i < appNames.size(); i++) {
            int duration = Math.max(1, minutes / (i + 2));
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(appNames.get(i));
            name.setPrefWidth(100);
            name.getStyleClass().add("analytics-name");
            ProgressBar bar = new ProgressBar(Math.min(1, duration / (double) minutes));
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.getStyleClass().add("analytics-bar");
            HBox.setHgrow(bar, Priority.ALWAYS);
            Label value = new Label(duration + "m");
            value.getStyleClass().add("analytics-value");
            row.getChildren().addAll(name, bar, value);
            rows.getChildren().add(row);
        }
    }
}
