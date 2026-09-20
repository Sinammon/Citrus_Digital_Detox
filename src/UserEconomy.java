import java.time.LocalDate;

// Manages points, productive tracking, and exchange rates
public class UserEconomy {
    private int coins;
    private double totalProductiveSeconds;
    private double dailyProductiveSeconds;
    private double lifetimeProductiveSeconds;
    private int secondsPerCoin = 360;
    private double dailyGoalHours = 2.0;
    private LocalDate dailyMetricDate = LocalDate.now();
    private LocalDate coinResetDate = LocalDate.now();

    public UserEconomy() {
        this.coins = 0;
        this.totalProductiveSeconds = 0;
        this.dailyProductiveSeconds = 0;
        this.lifetimeProductiveSeconds = 0;
    }

    public synchronized void addProductiveTime(double seconds) {
        if (seconds <= 0) return;
        resetCoinsIfNewDay();
        resetDailyMetricIfNeeded();
        this.totalProductiveSeconds += seconds;
        this.dailyProductiveSeconds += seconds;
        this.lifetimeProductiveSeconds += seconds;
        if (this.totalProductiveSeconds >= secondsPerCoin) {
            int earnedCoins = (int) (this.totalProductiveSeconds / secondsPerCoin);
            this.coins += earnedCoins;
            this.totalProductiveSeconds %= secondsPerCoin;
        }
    }

    public synchronized boolean spendCoins(int amount) {
        if (amount < 0) return false;
        if (coins >= amount) { coins -= amount; return true; }
        return false;
    }

    public synchronized double getTotalProductiveMinutes() {
        resetDailyMetricIfNeeded();
        return dailyProductiveSeconds / 60.0;
    }

    public synchronized void setCoins(int coins) { this.coins = Math.max(0, coins); }
    public synchronized LocalDate getCoinResetDate() { return coinResetDate; }
    public synchronized void setCoinResetDate(LocalDate date) { coinResetDate = date == null ? LocalDate.now() : date; }
    public synchronized LocalDate getDailyMetricDate() { return dailyMetricDate; }
    public synchronized boolean restoreDailyMetricDate(LocalDate date) {
        boolean current = date != null && LocalDate.now().equals(date);
        dailyMetricDate = current ? date : LocalDate.now();
        if (!current) { totalProductiveSeconds = 0; dailyProductiveSeconds = 0; }
        return current;
    }
    public synchronized boolean resetCoinsIfNewDay() {
        LocalDate today = LocalDate.now();
        if (coinResetDate == null || !today.equals(coinResetDate)) {
            coins = 0;
            coinResetDate = today;
            return true;
        }
        return false;
    }
    public synchronized double getTotalProductiveSecondsRaw() { resetDailyMetricIfNeeded(); return totalProductiveSeconds; }
    public synchronized void setTotalProductiveSecondsRaw(double seconds) {
        this.totalProductiveSeconds = Math.max(0, seconds);
        this.dailyMetricDate = LocalDate.now();
    }
    public synchronized double getDailyProductiveSeconds() { resetDailyMetricIfNeeded(); return dailyProductiveSeconds; }
    public synchronized void setDailyProductiveSeconds(double seconds) {
        this.dailyProductiveSeconds = Math.max(0, seconds);
        this.dailyMetricDate = LocalDate.now();
    }
    public synchronized double getLifetimeProductiveSeconds() { return lifetimeProductiveSeconds; }
    public synchronized void setLifetimeProductiveSeconds(double seconds) { this.lifetimeProductiveSeconds = Math.max(0, seconds); }
    public synchronized int getSecondsPerCoin() { return secondsPerCoin; }
    public synchronized void setSecondsPerCoin(int secondsPerCoin) { if (secondsPerCoin > 0) this.secondsPerCoin = secondsPerCoin; }
    public synchronized double getDailyGoalHours() { return dailyGoalHours; }
    public synchronized void setDailyGoalHours(double hours) { dailyGoalHours = Math.max(1.0, Math.min(10.0, hours)); }
    public synchronized double getDailyGoalMinutes() { return dailyGoalHours * 60.0; }
    public synchronized int getCoins() { return coins; }
    public synchronized double getRemainingSecondsToNextCoin() { return secondsPerCoin - totalProductiveSeconds; }
    public synchronized void resetDailyMetrics() {
        totalProductiveSeconds = 0;
        dailyProductiveSeconds = 0;
        dailyMetricDate = LocalDate.now();
        resetCoinsIfNewDay();
    }

    private void resetDailyMetricIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(dailyMetricDate)) {
            totalProductiveSeconds = 0;
            dailyProductiveSeconds = 0;
            dailyMetricDate = today;
        }
    }
}
