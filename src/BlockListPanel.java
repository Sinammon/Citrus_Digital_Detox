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
import javafx.scene.shape.Line;
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
            Node icon = block.getTargetName().equalsIgnoreCase("instagram") ? createInstagramIcon(locked && !bypassed) : createLockIcon(locked && !bypassed);
            HBox details = new HBox(12, icon, new VBox(4, title, status)); details.setAlignment(Pos.CENTER_LEFT);
            setGraphic(details);
        }
    }

    private Node createInstagramIcon(boolean locked) {
        if (locked) return createClosedLockIcon(Color.web("#E0A900"));
        Rectangle camera = new Rectangle(27, 27, Color.TRANSPARENT); camera.setArcWidth(9); camera.setArcHeight(9); camera.setStroke(Color.web("#C13584")); camera.setStrokeWidth(4);
        Circle lens = new Circle(6, Color.TRANSPARENT); lens.setStroke(Color.web("#C13584")); lens.setStrokeWidth(3);
        Circle dot = new Circle(2.5, Color.web("#C13584")); dot.setTranslateX(8); dot.setTranslateY(-8);
        return new StackPane(camera, lens, dot);
    }

    private Node createLockIcon(boolean locked) { return locked ? createClosedLockIcon(Color.web("#E0A900")) : createOpenLockIcon(Color.web("#FDCC21")); }
    private Node createClosedLockIcon(Color accent) {
        Rectangle body = new Rectangle(24, 18, accent); body.setArcWidth(5); body.setArcHeight(5);
        Circle ring = new Circle(9, Color.TRANSPARENT); ring.setStroke(accent); ring.setStrokeWidth(4); ring.setTranslateY(-10);
        Circle keyhole = new Circle(2, Color.WHITE); keyhole.setTranslateY(1);
        return iconStack(body, ring, keyhole);
    }
    private Node createOpenLockIcon(Color accent) {
        Rectangle body = new Rectangle(24, 18, accent); body.setArcWidth(5); body.setArcHeight(5);
        Circle ring = new Circle(9, Color.TRANSPARENT); ring.setStroke(accent); ring.setStrokeWidth(4); ring.setTranslateY(-10); ring.setRotate(-35);
        Circle keyhole = new Circle(2, Color.WHITE); keyhole.setTranslateY(1);
        return iconStack(body, ring, keyhole);
    }
    private Node iconStack(Node... nodes) { StackPane icon = new StackPane(new Group(nodes)); icon.setPrefSize(34, 34); icon.setMinSize(34, 34); icon.setMaxSize(34, 34); return icon; }
    private String titleCase(String text) { return text == null || text.isBlank() ? "App" : Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase(); }
}
