import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

public class WindowMonitor {
    private BlockManager blockManager;
    private final Object overlayLock = new Object();
    private BlockOverlay currentOverlay;
    private boolean overlayCreationPending;
    private UserEconomy economy;
    private long lastTickTime = System.currentTimeMillis();
    private Timer monitoringTimer;

    public WindowMonitor(BlockManager blockManager, UserEconomy economy) {
        this.blockManager = blockManager;
        this.economy = economy;
    }
    public String getActiveWindowTitle() {
        char[] buffer = new char[1024]; // buffer to store the window title
        User32 user32 = User32.INSTANCE;   // asks windows to keep track of whats on focus
        HWND hwnd = user32.GetForegroundWindow(); // get the handle of the active window
        user32.GetWindowText(hwnd, buffer, 1024); // get the title of the active window
        return Native.toString(buffer); // convert the title to a string
    }
    public void startMonitoring() {
        if (monitoringTimer != null) return;

        monitoringTimer = new Timer("digital-detox-window-monitor", true);
        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                String title = getActiveWindowTitle();
                Block match = blockManager.findMatchingBlock(title);
                updateOverlay(match);
                long now = System.currentTimeMillis();
                double elapsedSeconds = (now - lastTickTime) / 1000.0;
                lastTickTime = now;
                blockManager.recordUsageForWindow(title, elapsedSeconds);

                if (match == null && getIdleSeconds() < 60) {
                    economy.addProductiveTime(elapsedSeconds);
                }
            }
        }, 0, 300);
    }

    public void stopMonitoring() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            monitoringTimer = null;
        }
    }

    private void updateOverlay(Block match) {
        if (match != null) {
            synchronized (overlayLock) {
                if (currentOverlay != null || overlayCreationPending) return;
                overlayCreationPending = true;
            }

            blockManager.recordTrigger(match);
            Platform.runLater(() -> {
                synchronized (overlayLock) {
                    if (!overlayCreationPending) return;
                    currentOverlay = new BlockOverlay(match, blockManager);
                    overlayCreationPending = false;
                }
            });
            return;
        }

        BlockOverlay overlayToClose;
        synchronized (overlayLock) {
            overlayCreationPending = false;
            overlayToClose = currentOverlay;
            currentOverlay = null;
        }
        if (overlayToClose != null) {
            Platform.runLater(overlayToClose::dispose);
        }
    }
    public long getIdleSeconds() {
        User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO(); // asks windows to keep track of idle time
        lastInputInfo.cbSize = lastInputInfo.size(); // size of the structure
        User32.INSTANCE.GetLastInputInfo(lastInputInfo); // get the last input time

        int lastInputTick = lastInputInfo.dwTime; //
        int currentTick = Kernel32.INSTANCE.GetTickCount();
        return (currentTick - lastInputTick) / 1000L;
    }


}
