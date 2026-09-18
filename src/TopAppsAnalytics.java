import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.Comparator;
import java.util.Map;

public class TopAppsAnalytics {
    private final BlockManager blockManager;
    private final VBox root = new VBox(12);
    private final VBox rows = new VBox(10);

    public TopAppsAnalytics(BlockManager blockManager) { this.blockManager = blockManager; }

    public Node getView() {
        root.getStyleClass().add("card");
        Label title = new Label("TOP APPS & WEBSITES"); title.getStyleClass().add("analytics-title");
        root.getChildren().setAll(title, rows);
        refresh();
        return root;
    }

    public void refresh() {
        Map<String, Long> usage = blockManager.getUsageSeconds();
        var topFive = usage.entrySet().stream().filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())).limit(5).toList();
        rows.getChildren().clear();
        if (topFive.isEmpty()) { root.setVisible(false); root.setManaged(false); return; }
        root.setVisible(true); root.setManaged(true);
        long maxSeconds = topFive.get(0).getValue();
        for (Map.Entry<String, Long> entry : topFive) {
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(titleCase(entry.getKey())); name.setPrefWidth(120); name.getStyleClass().add("analytics-name");
            ProgressBar bar = new ProgressBar(entry.getValue() / (double) maxSeconds); bar.setMaxWidth(Double.MAX_VALUE); bar.getStyleClass().add("analytics-bar"); HBox.setHgrow(bar, Priority.ALWAYS);
            Label value = new Label(formatDuration(entry.getValue())); value.getStyleClass().add("analytics-value");
            row.getChildren().addAll(name, bar, value); rows.getChildren().add(row);
        }
    }

    private String formatDuration(long seconds) {
        long minutes = seconds / 60;
        return minutes > 0 ? minutes + "m" : Math.max(1, seconds) + "s";
    }
    private String titleCase(String value) { return value == null || value.isBlank() ? "App" : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(); }
}
