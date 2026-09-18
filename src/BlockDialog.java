import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class BlockDialog extends Dialog<Block> {
    private final TextField name = new TextField();
    private final ComboBox<LockType> type = new ComboBox<>(FXCollections.observableArrayList(LockType.values()));
    private final Label typeDescription = new Label();
    private final VBox options = new VBox(10);
    private final Spinner<Integer> timer = new Spinner<>(1, 1440, 10);
    private final Spinner<Integer> bedStart = new Spinner<>(0, 23, 22);
    private final Spinner<Integer> bedEnd = new Spinner<>(0, 23, 6);
    private final Spinner<Integer> challenge = new Spinner<>(5, 200, 20, 5);
    private final Spinner<Integer> delay = new Spinner<>(5, 600, 30, 5);
    private final PasswordField password = new PasswordField();
    private final Map<DayOfWeek, CheckBox> dayChecks = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, Spinner<Integer>[]> dayTimes = new EnumMap<>(DayOfWeek.class);

    public BlockDialog(BlockManager blockManager) {
        setTitle("Create a block");
        setHeaderText("Choose what Citrus should help you avoid.");
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        type.setValue(LockType.TIMER);
        type.setConverter(new javafx.util.StringConverter<>() {
            public String toString(LockType value) { return value == null ? "" : titleCase(value.name().replace('_', ' ')); }
            public LockType fromString(String value) { return LockType.valueOf(value.toUpperCase().replace(' ', '_')); }
        });
        type.valueProperty().addListener((obs, oldType, newType) -> refreshOptions());

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12); form.setPadding(new Insets(10));
        form.addRow(0, new Label("Website or app"), name);
        form.addRow(1, new Label("Lock type"), type);
        typeDescription.getStyleClass().add("muted");
        typeDescription.setWrapText(true);
        form.add(typeDescription, 1, 2);
        form.add(options, 0, 3, 2, 1);
        getDialogPane().setContent(form);
        refreshOptions();
        setResultConverter(button -> button == ButtonType.OK ? createBlock() : null);
    }

    private void refreshOptions() {
        options.getChildren().clear();
        typeDescription.setText(description(type.getValue()));
        switch (type.getValue()) {
            case TIMER -> options.getChildren().add(row("Duration (minutes)", timer));
            case TIME_RANGE -> options.getChildren().add(createScheduleEditor());
            case BEDTIME -> options.getChildren().addAll(row("Sleep hour", bedStart), row("Wake hour", bedEnd));
            case RANDOM_TEXT -> options.getChildren().add(row("Different words", challenge));
            case DELAY -> options.getChildren().add(row("Delay (seconds)", delay));
            case EMERGENCY -> options.getChildren().add(row("Emergency password", password));
        }
    }

    private VBox createScheduleEditor() {
        VBox schedule = new VBox(8);
        Label heading = new Label("Active days and hours");
        heading.getStyleClass().add("section-title");
        schedule.getChildren().add(heading);
        for (DayOfWeek day : DayOfWeek.values()) {
            CheckBox check = dayChecks.computeIfAbsent(day, d -> new CheckBox(titleCase(d.name())));
            check.setSelected(true);
            Spinner<Integer> start = new Spinner<>(0, 23, 9);
            Spinner<Integer> end = new Spinner<>(0, 23, 17);
            @SuppressWarnings("unchecked") Spinner<Integer>[] times = new Spinner[]{start, end};
            dayTimes.put(day, times);
            HBox row = new HBox(8, check, new Label("from"), start, new Label("to"), end);
            row.setPadding(new Insets(2, 0, 2, 0));
            schedule.getChildren().add(row);
            check.selectedProperty().addListener((obs, oldValue, selected) -> row.setDisable(!selected));
        }
        return schedule;
    }

    private HBox row(String label, javafx.scene.Node field) { return new HBox(12, new Label(label), field); }

    private Block createBlock() {
        if (name.getText().isBlank()) { warning("Enter a website or app name."); return null; }
        LockType selected = type.getValue();
        Block block = new Block(name.getText().trim(), selected);
        switch (selected) {
            case TIMER -> block.setUnlockAt(LocalDateTime.now().plusMinutes(timer.getValue()));
            case TIME_RANGE -> {
                Map<DayOfWeek, Block.DaySchedule> schedule = new EnumMap<>(DayOfWeek.class);
                EnumSet<DayOfWeek> selectedDays = EnumSet.noneOf(DayOfWeek.class);
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (!dayChecks.get(day).isSelected()) continue;
                    int start = dayTimes.get(day)[0].getValue();
                    int end = dayTimes.get(day)[1].getValue();
                    if (start == end) { warning("Start and end hours must be different for " + titleCase(day.name()) + "."); return null; }
                    selectedDays.add(day);
                    schedule.put(day, new Block.DaySchedule(LocalTime.of(start, 0), LocalTime.of(end, 0)));
                }
                if (selectedDays.isEmpty()) { warning("Select at least one active day."); return null; }
                block.setActiveDays(selectedDays);
                block.setDailySchedules(schedule);
                block.setRangeStart(schedule.get(selectedDays.iterator().next()).getStart());
                block.setRangeEnd(schedule.get(selectedDays.iterator().next()).getEnd());
            }
            case BEDTIME -> { block.setBedStart(LocalTime.of(bedStart.getValue(), 0)); block.setBedEnd(LocalTime.of(bedEnd.getValue(), 0)); }
            case RANDOM_TEXT -> block.setChallengeLength(challenge.getValue());
            case DELAY -> block.setDelaySeconds(delay.getValue());
            case EMERGENCY -> { if (password.getText().isBlank()) { warning("Enter an emergency password."); return null; } block.setEmergencyPassword(password.getText()); }
        }
        return block;
    }

    private String description(LockType lockType) {
        return switch (lockType) {
            case TIMER -> "Blocks the app until a countdown finishes.";
            case TIME_RANGE -> "Blocks the app only during the selected hours on selected days.";
            case RANDOM_TEXT -> "Requires a unique word challenge before the block can be bypassed.";
            case DELAY -> "Adds a waiting period before the app can be opened.";
            case BEDTIME -> "Blocks the app during your chosen sleep hours.";
            case EMERGENCY -> "Requires your emergency password to bypass the block.";
        };
    }

    private String titleCase(String text) {
        String[] words = text.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) if (!word.isBlank()) result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        return result.toString().trim();
    }

    private void warning(String message) { new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait(); }
}
