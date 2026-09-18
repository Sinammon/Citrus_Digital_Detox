import java.time.LocalDate;

// Manages points, productive tracking, and exchange rates
public class UserEconomy {
    private int coins;
    private double totalProductiveSeconds;
    private double dailyProductiveSeconds;
    private double lifetimeProductiveSeconds;
    private int secondsPerCoin = 360;
    private LocalDate dailyMetricDate = LocalDate.now();

    public UserEconomy() {
        this.coins = 0;
        this.totalProductiveSeconds = 0;
        this.dailyProductiveSeconds = 0;
        this.lifetimeProductiveSeconds = 0;
    }

    public synchronized void addProductiveTime(double seconds) {
        if (seconds <= 0) return;
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
    public synchronized int getCoins() { return coins; }
    public synchronized double getRemainingSecondsToNextCoin() { return secondsPerCoin - totalProductiveSeconds; }
    public synchronized void resetDailyMetrics() {
        totalProductiveSeconds = 0;
        dailyProductiveSeconds = 0;
        dailyMetricDate = LocalDate.now();
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
