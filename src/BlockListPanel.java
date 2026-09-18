import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BlockListPanel {
    private final BlockManager blockManager;
    private final ObservableList<Block> blocks = FXCollections.observableArrayList();
    private final ListView<Block> list = new ListView<>(blocks);

    public BlockListPanel(BlockManager blockManager) {
        this.blockManager = blockManager;
        list.setCellFactory(view -> new BlockCell());
        refresh();
        Timeline timer = new Timeline(new KeyFrame(Duration.millis(250), event -> refresh()));
        timer.setCycleCount(Timeline.INDEFINITE); timer.play();
    }

    public Node getView() {
        BorderPane root = new BorderPane(); root.setCenter(list); root.setBottom(createActions()); root.getStyleClass().add("content"); return root;
    }

    private Node createActions() {
        Button create = new Button("+ Create block"); create.getStyleClass().add("primary-button");
        create.setOnAction(event -> new BlockDialog(blockManager).showAndWait().ifPresent(block -> { blockManager.addBlock(block); refresh(); }));
        Button delete = new Button("Delete selected"); delete.getStyleClass().add("secondary-button");
        delete.setOnAction(event -> { Block block = list.getSelectionModel().getSelectedItem(); if (block != null) blockManager.removeBlock(block); refresh(); });
        HBox actions = new HBox(10, create, delete); actions.setAlignment(Pos.CENTER_LEFT); return actions;
    }

    private void refresh() {
        Block selected = list.getSelectionModel().getSelectedItem();
        blocks.setAll(blockManager.getBlocks());
        if (selected != null) list.getSelectionModel().select(selected);
        list.refresh();
    }

    private class BlockCell extends ListCell<Block> {
        @Override protected void updateItem(Block block, boolean empty) {
            super.updateItem(block, empty);
            if (empty || block == null) { setGraphic(null); return; }
            boolean locked = block.isCurrentlyBlocking();
            boolean bypassed = blockManager.isBypassedWithPass(block);
            String state = bypassed ? "Unlocked block with pass" : locked ? "Locked" : block.isActive() ? "Enabled" : "Disabled";
            Label title = new Label(titleCase(block.getTargetName())); title.getStyleClass().add("section-title");
            Label status = new Label(state + " • " + titleCase(block.getLockType().name().replace('_', ' '))); status.getStyleClass().add("muted");
            HBox details = new HBox(12, createClosedLockIcon(), new VBox(4, title, status)); details.setAlignment(Pos.CENTER_LEFT);
            setGraphic(details);
        }
    }

    /** One stable, closed lock illustration used for every block regardless of runtime state. */
    private Node createClosedLockIcon() {
        Rectangle body = new Rectangle(26, 20, Color.web("#FFAD32"));
        body.setArcWidth(7); body.setArcHeight(7);
        Circle shackle = new Circle(10, Color.TRANSPARENT);
        shackle.setStroke(Color.web("#AAB7C0")); shackle.setStrokeWidth(6); shackle.setTranslateY(-10);
        Circle keyhole = new Circle(2.3, Color.WHITE); keyhole.setTranslateY(1);
        StackPane icon = new StackPane(new Group(body, shackle, keyhole));
        icon.setPrefSize(38, 38); icon.setMinSize(38, 38); icon.setMaxSize(38, 38);
        return icon;
    }

    private String titleCase(String text) { return text == null || text.isBlank() ? "App" : Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase(); }
}
