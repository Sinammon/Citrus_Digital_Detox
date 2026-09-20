import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsPanel {
    private final BlockManager blockManager;
    private final UserEconomy economy;
    private final ProgressBar progress = new ProgressBar();
    private final PieChart chart = new PieChart();
    private final ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
    private final VBox legend = new VBox(10);
    private final VBox legendPanel = new VBox(legend);
    private final String[] palette = {"#F59E0B", "#2563EB", "#16A34A", "#DC2626", "#9333EA", "#0891B2", "#DB2777", "#4F46E5"};
    private final List<Label> legendLabels = new ArrayList<>();

    public StatisticsPanel(BlockManager blockManager, UserEconomy economy) { this.blockManager = blockManager; this.economy = economy; }

    public Node getView() {
        VBox root = new VBox(18); root.getStyleClass().add("content");
        Label goal = new Label("Daily focus goal"); goal.getStyleClass().add("section-title"); progress.setMaxWidth(Double.MAX_VALUE);
        Label hint = new Label("Progress toward 2 hours of productive time"); hint.getStyleClass().add("muted");
        chart.setTitle("Lock Activity : Blocks Triggered"); chart.setLegendVisible(false); chart.setLabelsVisible(false); chart.setAnimated(false); chart.setData(chartData); VBox.setVgrow(chart, Priority.ALWAYS);
        legendPanel.getStyleClass().add("chart-legend-panel");
        legendPanel.setPadding(new Insets(12)); legendPanel.setSpacing(0); legendPanel.setMaxHeight(Region.USE_PREF_SIZE); legendPanel.setMinHeight(Region.USE_PREF_SIZE);
        HBox visualization = new HBox(24, chart, legendPanel); visualization.setAlignment(Pos.CENTER); HBox.setHgrow(chart, Priority.ALWAYS); VBox.setVgrow(visualization, Priority.ALWAYS);
        root.getChildren().addAll(goal, progress, hint, visualization); refresh();
        Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(1), e -> refresh())); refresh.setCycleCount(Timeline.INDEFINITE); refresh.play(); return root;
    }

    private void refresh() {
        progress.setProgress(Math.min(economy.getTotalProductiveMinutes() / 120.0, 1));
        var counts = blockManager.getTriggerCounts();
        var activeCounts = counts.entrySet().stream().filter(entry -> entry.getValue() > 0).toList();
        if (needsRebuild(activeCounts)) rebuildChart(activeCounts);
        else updateExistingValues(activeCounts);
        if (chartData.isEmpty()) { legendPanel.setVisible(false); legendPanel.setManaged(false); }
        else { legendPanel.setVisible(true); legendPanel.setManaged(true); }
    }

    private boolean needsRebuild(List<? extends Map.Entry<String, Integer>> activeCounts) {
        if (chartData.size() != activeCounts.size()) return true;
        for (int i = 0; i < activeCounts.size(); i++) if (!chartData.get(i).getName().equals(activeCounts.get(i).getKey())) return true;
        return false;
    }

    private void rebuildChart(List<? extends Map.Entry<String, Integer>> activeCounts) {
        chartData.clear(); legend.getChildren().clear(); legendLabels.clear();
        for (int i = 0; i < activeCounts.size(); i++) {
            Map.Entry<String, Integer> entry = activeCounts.get(i);
            chartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            String color = palette[i % palette.length];
            Rectangle swatch = new Rectangle(12, 12, Color.web(color));
            Label label = new Label(); label.getStyleClass().add("muted"); legendLabels.add(label); updateLegendLabel(label, entry.getKey(), entry.getValue());
            HBox row = new HBox(8, swatch, label); row.setAlignment(Pos.CENTER_LEFT); legend.getChildren().add(row);
        }
        Platform.runLater(this::applySliceColors);
        legendPanel.applyCss(); legendPanel.requestLayout();
    }

    private void updateExistingValues(List<? extends Map.Entry<String, Integer>> activeCounts) {
        for (int i = 0; i < activeCounts.size(); i++) {
            Map.Entry<String, Integer> entry = activeCounts.get(i);
            PieChart.Data slice = chartData.get(i);
            if (slice.getPieValue() != entry.getValue()) slice.setPieValue(entry.getValue());
            updateLegendLabel(legendLabels.get(i), entry.getKey(), entry.getValue());
        }
    }

    private void updateLegendLabel(Label label, String name, int count) {
        label.setText(titleCase(name) + " (Triggered: " + count + ")");
    }

    private void applySliceColors() {
        for (int i = 0; i < chartData.size(); i++) {
            Node slice = chartData.get(i).getNode();
            if (slice != null) slice.setStyle("-fx-pie-color: " + palette[i % palette.length] + ";");
        }
    }
    private String titleCase(String value) { return value == null || value.isBlank() ? "App" : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(); }
}
