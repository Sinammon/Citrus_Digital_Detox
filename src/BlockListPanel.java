import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
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
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class BlockListPanel {
    private final BlockManager blockManager;
    private final ObservableList<Block> blocks = FXCollections.observableArrayList();
    private final ListView<Block> list = new ListView<>(blocks);

    public BlockListPanel(BlockManager blockManager) {
        this.blockManager = blockManager;
        list.setCellFactory(view -> new BlockCell());
        syncListModel();
        Timeline timer = new Timeline(new KeyFrame(Duration.millis(500), event -> refreshVisibleCells()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    public Node getView() {
        BorderPane root = new BorderPane();
        root.setCenter(list);
        root.setBottom(createActions());
        root.getStyleClass().add("content");
        return root;
    }

    private Node createActions() {
        Button create = new Button("+ Create block");
        create.getStyleClass().add("primary-button");
        create.setOnAction(event -> new BlockDialog(blockManager).showAndWait().ifPresent(block -> {
            blockManager.addBlock(block);
            syncListModel();
        }));

        Button delete = new Button("Delete selected");
        delete.getStyleClass().add("secondary-button");
        delete.setOnAction(event -> {
            Block block = list.getSelectionModel().getSelectedItem();
            if (block != null) {
                blockManager.removeBlock(block);
                syncListModel();
            }
        });
        HBox actions = new HBox(10, create, delete);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    /** Replaces the model only when blocks are added or removed, preserving selection during timer updates. */
    private void syncListModel() {
        Block selected = list.getSelectionModel().getSelectedItem();
        blocks.setAll(blockManager.getBlocks());
        if (selected != null && blocks.contains(selected)) list.getSelectionModel().select(selected);
    }

    /** Refreshes cell content in place; it never clears or rebuilds the list model. */
    private void refreshVisibleCells() {
        list.refresh();
    }

    private class BlockCell extends ListCell<Block> {
        @Override protected void updateItem(Block block, boolean empty) {
            super.updateItem(block, empty);
            if (empty || block == null) {
                setGraphic(null);
                return;
            }
            boolean locked = block.isCurrentlyBlocking();
            boolean bypassed = blockManager.isBypassedWithPass(block);
            String state = bypassed ? "Unlocked block with pass" : locked ? "Locked" : block.isTimerExpired() ? "Disabled" : block.isActive() ? "Enabled" : "Disabled";
            Label title = new Label(titleCase(block.getTargetName()));
            title.getStyleClass().add("section-title");
            Label status = new Label(state + " • " + titleCase(block.getLockType().name().replace('_', ' ')));
            status.getStyleClass().add("muted");
            HBox details = new HBox(12, createStaticLockIcon(), new VBox(4, title, status));
            details.setAlignment(Pos.CENTER_LEFT);
            setGraphic(details);
        }
    }

    /** Static standard padlock: gray U-shaped shackle, orange rounded body, and white keyhole. */
    private Node createStaticLockIcon() {
        Rectangle body = new Rectangle(27, 21, Color.web("#FFAD32"));
        body.setArcWidth(7);
        body.setArcHeight(7);
        SVGPath shackle = new SVGPath();
        shackle.setContent("M 8 15 L 8 9 A 9 9 0 0 1 26 9 L 26 15 L 21 15 L 21 9 A 4 4 0 0 0 13 9 L 13 15 Z");
        shackle.setFill(Color.web("#AAB7C0"));
        Circle keyhole = new Circle(2.3, Color.WHITE);
        keyhole.setTranslateY(1);
        StackPane icon = new StackPane(body, shackle, keyhole);
        icon.setPrefSize(38, 38);
        icon.setMinSize(38, 38);
        icon.setMaxSize(38, 38);
        return icon;
    }

    private String titleCase(String text) {
        return text == null || text.isBlank() ? "App" : Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
}
