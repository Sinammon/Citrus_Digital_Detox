import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
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
    private final VBox options = new VBox(12);
    private final Slider timer = slider(1, 1440, 10, 1);
    private final Slider bedStart = slider(0, 23, 22, 1);
    private final Slider bedEnd = slider(0, 23, 6, 1);
    private final Slider challenge = slider(5, 200, 20, 5);
    private final Slider delay = slider(5, 600, 30, 5);
    private final Map<DayOfWeek, CheckBox> dayChecks = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, Slider[]> dayTimes = new EnumMap<>(DayOfWeek.class);
    private final PasswordField password = new PasswordField();

    public BlockDialog(BlockManager blockManager) {
        setTitle("Create a block");
        setHeaderText("Choose what Citrus should help you avoid.");
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        getDialogPane().setPrefWidth(760);
        getDialogPane().setPrefHeight(640);
        type.setValue(LockType.TIMER);
        type.setPrefWidth(240);
        type.setConverter(new javafx.util.StringConverter<>() {
            public String toString(LockType value) { return value == null ? "" : titleCase(value.name().replace('_', ' ')); }
            public LockType fromString(String value) { return LockType.valueOf(value.toUpperCase().replace(' ', '_')); }
        });
        type.valueProperty().addListener((obs, oldType, newType) -> refreshOptions());

        GridPane form = new GridPane();
        form.setHgap(18); form.setVgap(16); form.setPadding(new Insets(24));
        form.setMinWidth(700);
        name.setPrefWidth(430);
        form.addRow(0, new Label("Website or app"), name);
        form.addRow(1, new Label("Lock type"), type);
        typeDescription.getStyleClass().add("muted");
        typeDescription.setWrapText(true);
        typeDescription.setMaxWidth(520);
        form.add(typeDescription, 1, 2);
        form.add(options, 0, 3, 2, 1);
        getDialogPane().setContent(form);
        refreshOptions();
        setResultConverter(button -> button == ButtonType.OK ? createBlock() : null);
    }

    private Slider slider(double min, double max, double value, double step) {
        Slider slider = new Slider(min, max, value);
        slider.setBlockIncrement(step);
        slider.setMajorTickUnit(step);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(false);
        slider.setPrefWidth(360);
        return slider;
    }

    private HBox sliderRow(String label, Slider slider, String suffix) {
        Label value = new Label();
        value.getStyleClass().add("slider-value");
        Runnable update = () -> value.setText(String.valueOf(Math.round(slider.getValue())) + suffix);
        slider.valueProperty().addListener((obs, oldValue, newValue) -> update.run());
        update.run();
        HBox row = new HBox(14, new Label(label), slider, value);
        row.setPadding(new Insets(4, 0, 4, 0));
        return row;
    }

    private void refreshOptions() {
        options.getChildren().clear();
        typeDescription.setText(description(type.getValue()));
        switch (type.getValue()) {
            case TIMER -> options.getChildren().add(sliderRow("Duration", timer, " min"));
            case TIME_RANGE -> options.getChildren().add(createScheduleEditor());
            case BEDTIME -> options.getChildren().addAll(sliderRow("Sleep hour", bedStart, ":00"), sliderRow("Wake hour", bedEnd, ":00"));
            case RANDOM_TEXT -> options.getChildren().add(sliderRow("Different words", challenge, " words"));
            case DELAY -> options.getChildren().add(sliderRow("Delay", delay, " sec"));
            case EMERGENCY -> options.getChildren().add(new HBox(14, new Label("Emergency password"), password));
        }
    }

    private VBox createScheduleEditor() {
        VBox schedule = new VBox(8);
        Label heading = new Label("Active days and hours");
        heading.getStyleClass().add("section-title");
        schedule.getChildren().add(heading);
        for (DayOfWeek day : DayOfWeek.values()) {
            CheckBox check = dayChecks.computeIfAbsent(day, d -> new CheckBox(shortDay(d)));
            check.setSelected(true);
            Slider start = slider(0, 23, 9, 1);
            Slider end = slider(0, 23, 17, 1);
            dayTimes.put(day, new Slider[]{start, end});
            HBox row = new HBox(8, check, sliderRow("from", start, ":00"), sliderRow("to", end, ":00"));
            row.setPadding(new Insets(2, 0, 2, 0));
            schedule.getChildren().add(row);
            check.selectedProperty().addListener((obs, oldValue, selected) -> row.setDisable(!selected));
        }
        return schedule;
    }

    private Block createBlock() {
        if (name.getText().isBlank()) { warning("Enter a website or app name."); return null; }
        LockType selected = type.getValue();
        Block block = new Block(titleCase(name.getText().trim()), selected);
        switch (selected) {
            case TIMER -> block.setUnlockAt(LocalDateTime.now().plusMinutes(Math.round(timer.getValue())));
            case TIME_RANGE -> {
                Map<DayOfWeek, Block.DaySchedule> schedule = new EnumMap<>(DayOfWeek.class);
                EnumSet<DayOfWeek> selectedDays = EnumSet.noneOf(DayOfWeek.class);
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (!dayChecks.get(day).isSelected()) continue;
                    int start = (int) Math.round(dayTimes.get(day)[0].getValue());
                    int end = (int) Math.round(dayTimes.get(day)[1].getValue());
                    if (start == end) { warning("Start and end hours must be different for " + shortDay(day) + "."); return null; }
                    selectedDays.add(day);
                    schedule.put(day, new Block.DaySchedule(LocalTime.of(start, 0), LocalTime.of(end, 0)));
                }
                if (selectedDays.isEmpty()) { warning("Select at least one active day."); return null; }
                block.setActiveDays(selectedDays);
                block.setDailySchedules(schedule);
                block.setRangeStart(schedule.get(selectedDays.iterator().next()).getStart());
                block.setRangeEnd(schedule.get(selectedDays.iterator().next()).getEnd());
            }
            case BEDTIME -> { block.setBedStart(LocalTime.of((int) Math.round(bedStart.getValue()), 0)); block.setBedEnd(LocalTime.of((int) Math.round(bedEnd.getValue()), 0)); }
            case RANDOM_TEXT -> block.setChallengeLength((int) Math.round(challenge.getValue()));
            case DELAY -> block.setDelaySeconds((int) Math.round(delay.getValue()));
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

    private String shortDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Mon"; case TUESDAY -> "Tue"; case WEDNESDAY -> "Wed";
            case THURSDAY -> "Thu"; case FRIDAY -> "Fri"; case SATURDAY -> "Sat"; case SUNDAY -> "Sun";
        };
    }

    private void warning(String message) { new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait(); }
}
