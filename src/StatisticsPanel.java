import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StatisticsPanel {
    private final BlockManager blockManager;
    private final UserEconomy economy;
    private final ProgressBar progress = new ProgressBar();
    private final PieChart chart = new PieChart();

    public StatisticsPanel(BlockManager blockManager, UserEconomy economy) {
        this.blockManager = blockManager;
        this.economy = economy;
    }

    public Node getView() {
        VBox root = new VBox(18);
        root.getStyleClass().add("content");
        Label goal = new Label("Daily focus goal"); goal.getStyleClass().add("section-title");
        progress.setMaxWidth(Double.MAX_VALUE);
        Label hint = new Label("Progress toward 2 hours of productive time"); hint.getStyleClass().add("muted");
        chart.setTitle("Lock activity proportions");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setAnimated(false);
        VBox.setVgrow(chart, javafx.scene.layout.Priority.ALWAYS);
        root.getChildren().addAll(goal, progress, hint, chart);
        refresh();
        Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(1), e -> refresh()));
        refresh.setCycleCount(Timeline.INDEFINITE); refresh.play();
        return root;
    }

    private void refresh() {
        progress.setProgress(Math.min(economy.getTotalProductiveMinutes() / 120.0, 1));
        chart.setData(FXCollections.observableArrayList(blockManager.getBlocks().stream()
                .filter(block -> block.getTimesTriggered() > 0)
                .map(block -> new PieChart.Data(block.getTargetName(), block.getTimesTriggered()))
                .toList()));
    }
}
