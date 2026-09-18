import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

public class MainDashboard {
    private final BlockManager blockManager;
    private final UserEconomy economy;
    private final BorderPane root = new BorderPane();
    private final StackPane content = new StackPane();
    private final Label pageTitle = new Label("Dashboard");
    private final Label coinLabel = new Label();
    private final ToggleGroup navigation = new ToggleGroup();

    public MainDashboard(BlockManager blockManager, UserEconomy economy) {
        this.blockManager = blockManager;
        this.economy = economy;
        root.getStyleClass().add("root");
        root.setLeft(createSidebar());
        root.setCenter(content);
        showPage("Dashboard", createDashboard());

        Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(1), event ->
                coinLabel.setText(String.valueOf(economy.getCoins()))));
        refresh.setCycleCount(Timeline.INDEFINITE);
        refresh.play();
    }

    public BorderPane getView() { return root; }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        HBox brandRow = new HBox(10, createLemonLogo(32), new Label("Citrus"));
        brandRow.setAlignment(Pos.CENTER_LEFT);
        brandRow.getStyleClass().add("brand-row");
        brandRow.lookupAll(".label").forEach(node -> node.getStyleClass().add("brand"));

        Label tagline = new Label("Digital detox, made simple");
        tagline.getStyleClass().add("muted");
        VBox.setMargin(tagline, new Insets(0, 0, 26, 0));

        ToggleButton dashboard = navButton("⌂  Dashboard", true, () -> showPage("Dashboard", createDashboard()));
        ToggleButton blocks = navButton("◫  My Blocks", false, () -> showPage("My Blocks", new BlockListPanel(blockManager).getView()));
        ToggleButton shop = navButton("◈  Shop", false, () -> showPage("Shop", new ShopPanel(blockManager, economy).getView()));
        ToggleButton statistics = navButton("↗  Statistics", false, () -> showPage("Statistics", new StatisticsPanel(blockManager, economy).getView()));
        ToggleButton settings = navButton("⚙  Settings", false, () -> showPage("Settings", new SettingsPanel(economy).getView()));

        sidebar.getChildren().addAll(brandRow, tagline, dashboard, blocks, shop, statistics);
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(spacer, settings);
        return sidebar;
    }

    private ToggleButton navButton(String text, boolean selected, Runnable action) {
        ToggleButton button = new ToggleButton(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setToggleGroup(navigation);
        button.setSelected(selected);
        button.setOnAction(event -> action.run());
        return button;
    }

    private Node createPageFrame(String title, Node page) {
        VBox frame = new VBox(18);
        frame.getStyleClass().add("page-frame");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        pageTitle.setText(title);
        pageTitle.getStyleClass().add("page-title");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(pageTitle, spacer, createCoinDisplay());

        VBox.setVgrow(page, Priority.ALWAYS);
        frame.getChildren().addAll(header, page);
        return frame;
    }

    private HBox createCoinDisplay() {
        HBox display = new HBox(9, createCoinIcon(22), coinLabel);
        display.setAlignment(Pos.CENTER);
        display.getStyleClass().add("coin-chip");
        coinLabel.getStyleClass().add("coin-value");
        return display;
    }

    private void showPage(String title, Node page) {
        content.getChildren().setAll(createPageFrame(title, page));
    }

    private Node createLemonLogo(double size) {
        double radius = size / 2;
        Circle rind = new Circle(radius, Color.web("#FDCC21"));
        rind.setStroke(Color.web("#E0A900"));
        rind.setStrokeWidth(2);
        Circle flesh = new Circle(radius * 0.78, Color.web("#FFF8D9"));
        flesh.setStroke(Color.web("#FFFFFF"));
        flesh.setStrokeWidth(1.5);

        Group segments = new Group();
        for (int i = 0; i < 6; i++) {
            Arc segment = new Arc(0, 0, radius * 0.68, radius * 0.68, i * 60 + 4, 52);
            segment.setType(ArcType.ROUND);
            segment.setFill(Color.web(i % 2 == 0 ? "#F8C51A" : "#FFE578"));
            segments.getChildren().add(segment);
        }
        StackPane logo = new StackPane(rind, flesh, segments);
        logo.setPrefSize(size, size);
        logo.setMinSize(size, size);
        logo.setMaxSize(size, size);
        return logo;
    }

    private Node createCoinIcon(double size) {
        double radius = size / 2;
        Circle coin = new Circle(radius, Color.web("#FDCC21"));
        coin.setStroke(Color.web("#B98200"));
        coin.setStrokeWidth(1.5);
        Circle innerRing = new Circle(radius * 0.68, Color.TRANSPARENT);
        innerRing.setStroke(Color.web("#FFF8D9"));
        innerRing.setStrokeWidth(1.5);
        Polygon sparkle = new Polygon(0, -radius * 0.38, radius * 0.1, -radius * 0.1,
                radius * 0.38, 0, radius * 0.1, radius * 0.1, 0, radius * 0.38,
                -radius * 0.1, radius * 0.1, -radius * 0.38, 0, -radius * 0.1, -radius * 0.1);
        sparkle.setFill(Color.web("#FFF8D9"));
        StackPane icon = new StackPane(coin, innerRing, sparkle);
        icon.setPrefSize(size, size);
        icon.setMinSize(size, size);
        icon.setMaxSize(size, size);
        return icon;
    }

    private Node createDashboard() {
        VBox view = new VBox(22);
        view.getStyleClass().add("content");
        Label subtitle = new Label("Set boundaries for distracting apps, then earn coins while you stay focused.");
        subtitle.getStyleClass().add("hero-title");
        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(metricCard("Productive time", "time"), metricCard("Blocks active now", "blocks"),
                staticCard("Daily goal", "2h 0m", "A gentle target for today"));
        TutorialContent tutorial = new TutorialContent();
        view.getChildren().addAll(subtitle, metrics, tutorial.getView());
        Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int minutes = (int) economy.getTotalProductiveMinutes();
            ((Label) metrics.lookup("#time")).setText((minutes / 60) + "h " + (minutes % 60) + "m");
            ((Label) metrics.lookup("#blocks")).setText(String.valueOf(blockManager.countActiveBlocks()));
        }));
        refresh.setCycleCount(Timeline.INDEFINITE);
        refresh.play();
        return view;
    }

    private VBox metricCard(String title, String id) {
        VBox card = staticCard(title, "0", "Live update");
        ((Label) card.lookup(".metric-value")).setId(id);
        return card;
    }

    private VBox staticCard(String title, String value, String detail) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(230);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label label = new Label(title);
        label.getStyleClass().add("metric-label");
        Label metric = new Label(value);
        metric.getStyleClass().add("metric-value");
        Label description = new Label(detail);
        description.getStyleClass().add("muted");
        card.getChildren().addAll(label, metric, description);
        return card;
    }
}
