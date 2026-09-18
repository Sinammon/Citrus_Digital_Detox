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
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class BlockListPanel {
    private final BlockManager blockManager;
    private final ObservableList<Block> blocks = FXCollections.observableArrayList();
    private final ListView<Block> list = new ListView<>(blocks);

    public BlockListPanel(BlockManager blockManager) {
        this.blockManager = blockManager;
        list.setCellFactory(view -> new BlockCell());
        refresh();
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> refresh()));
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
            refresh();
        }));
        Button delete = new Button("Delete selected");
        delete.getStyleClass().add("secondary-button");
        delete.setOnAction(event -> {
            Block block = list.getSelectionModel().getSelectedItem();
            if (block != null) blockManager.removeBlock(block);
            refresh();
        });
        HBox actions = new HBox(10, create, delete);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private void refresh() {
        Block selected = list.getSelectionModel().getSelectedItem();
        blocks.setAll(blockManager.getBlocks());
        if (selected != null) list.getSelectionModel().select(selected);
        list.refresh();
    }

    private static class BlockCell extends ListCell<Block> {
        @Override protected void updateItem(Block block, boolean empty) {
            super.updateItem(block, empty);
            if (empty || block == null) { setGraphic(null); return; }
            VBox details = new VBox(4);
            Label title = new Label(block.getTargetName());
            title.getStyleClass().add("section-title");
            Label status = new Label((block.isActive() ? "Enabled" : "Disabled") + " • " + block.getLockType() +
                    " • " + (block.isCurrentlyBlocking() ? "Blocking now" : "Not blocking"));
            status.getStyleClass().add("muted");
            details.getChildren().addAll(title, status);
            setGraphic(details);
        }
    }
}
