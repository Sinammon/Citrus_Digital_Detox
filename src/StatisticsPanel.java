import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class StatisticsPanel {
    private final BlockManager blockManager;
    private final UserEconomy economy;
    private final ProgressBar progress = new ProgressBar();
    private final PieChart chart = new PieChart();
    private final VBox legend = new VBox(10);
    private final VBox legendPanel = new VBox(legend);
    private final String[] palette = {"#F59E0B", "#2563EB", "#16A34A", "#DC2626", "#9333EA", "#0891B2", "#DB2777", "#4F46E5"};

    public StatisticsPanel(BlockManager blockManager, UserEconomy economy) { this.blockManager = blockManager; this.economy = economy; }

    public Node getView() {
        VBox root = new VBox(18); root.getStyleClass().add("content");
        Label goal = new Label("Daily focus goal"); goal.getStyleClass().add("section-title"); progress.setMaxWidth(Double.MAX_VALUE);
        Label hint = new Label("Progress toward 2 hours of productive time"); hint.getStyleClass().add("muted");
        chart.setTitle("Lock Activity : Blocks Triggered"); chart.setLegendVisible(false); chart.setLabelsVisible(false); chart.setAnimated(false); VBox.setVgrow(chart, Priority.ALWAYS);
        legendPanel.getStyleClass().add("chart-legend-panel");
        legendPanel.setPadding(new Insets(12)); legendPanel.setSpacing(0); legendPanel.setMaxHeight(Region.USE_PREF_SIZE); legendPanel.setMinHeight(Region.USE_PREF_SIZE);
        HBox visualization = new HBox(24, chart, legendPanel); visualization.setAlignment(Pos.CENTER); HBox.setHgrow(chart, Priority.ALWAYS); VBox.setVgrow(visualization, Priority.ALWAYS);
        root.getChildren().addAll(goal, progress, hint, visualization); refresh();
        Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(1), e -> refresh())); refresh.setCycleCount(Timeline.INDEFINITE); refresh.play(); return root;
    }

    private void refresh() {
        progress.setProgress(Math.min(economy.getTotalProductiveMinutes() / 120.0, 1));
        var counts = blockManager.getTriggerCounts();
        var data = FXCollections.observableArrayList(counts.entrySet().stream().filter(entry -> entry.getValue() > 0).map(entry -> new PieChart.Data(entry.getKey(), entry.getValue())).toList());
        chart.setData(data); chart.setLabelsVisible(false); legend.getChildren().clear();
        if (data.isEmpty()) { legendPanel.setVisible(false); legendPanel.setManaged(false); return; }
        legendPanel.setVisible(true); legendPanel.setManaged(true);
        for (int i = 0; i < data.size(); i++) {
            PieChart.Data slice = data.get(i);
            String color = palette[i % palette.length];
            Rectangle swatch = new Rectangle(12, 12, Color.web(color));
            Label label = new Label(titleCase(slice.getName()) + " (Triggered: " + (int) slice.getPieValue() + ")"); label.getStyleClass().add("muted");
            HBox row = new HBox(8, swatch, label); row.setAlignment(Pos.CENTER_LEFT); legend.getChildren().add(row);
        }
        Platform.runLater(() -> applySliceColors(data));
        legendPanel.applyCss(); legendPanel.requestLayout();
    }

    private void applySliceColors(java.util.List<PieChart.Data> data) {
        for (int i = 0; i < data.size(); i++) {
            Node slice = data.get(i).getNode();
            if (slice != null) slice.setStyle("-fx-pie-color: " + palette[i % palette.length] + ";");
        }
    }
    private String titleCase(String value) { return value == null || value.isBlank() ? "App" : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(); }
}
