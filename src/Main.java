import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private final BlockManager blockManager = new BlockManager();
    private final UserEconomy economy = new UserEconomy();
    private WindowMonitor monitor;
    private ScheduledExecutorService dailyReset;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        SaveManager.load(blockManager, economy);
        MainDashboard dashboard = new MainDashboard(blockManager, economy);
        Scene scene = new Scene(dashboard.getView(), 1100, 720);
        scene.getStylesheets().add(getClass().getResource("/citrus.css").toExternalForm());
        stage.setTitle("Citrus — Digital Detox"); stage.setMinWidth(900); stage.setMinHeight(620); stage.setScene(scene); stage.show();
        monitor = new WindowMonitor(blockManager, economy); monitor.startMonitoring();
        scheduleDailyReset();
    }

    private void scheduleDailyReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long initialDelay = Math.max(1, Duration.between(now, nextMidnight).toMillis());
        dailyReset = Executors.newSingleThreadScheduledExecutor(r -> { Thread thread = new Thread(r, "citrus-daily-reset"); thread.setDaemon(true); return thread; });
        dailyReset.scheduleAtFixedRate(() -> {
            blockManager.resetDailyMetrics();
            economy.resetDailyMetrics();
            SaveManager.save(blockManager, economy);
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (monitor != null) monitor.stopMonitoring();
        if (dailyReset != null) dailyReset.shutdownNow();
        SaveManager.save(blockManager, economy);
    }
}
