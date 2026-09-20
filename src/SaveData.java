import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Block> blocks;
    private List<Pass> passes;
    private Map<String, Long> usageSeconds;
    private Map<String, Integer> triggerCounts;
    private int coins;
    private double totalProductiveSeconds;
    private double dailyProductiveSeconds;
    private double lifetimeProductiveSeconds;
    private boolean hasLifetimeProductiveSeconds;
    private int secondsPerCoin;
    private LocalDate coinResetDate;
    private LocalDate metricDate;

    public SaveData(List<Block> blocks, List<Pass> passes, Map<String, Long> usageSeconds, Map<String, Integer> triggerCounts, int coins,
                    double totalProductiveSeconds, double dailyProductiveSeconds,
                    double lifetimeProductiveSeconds, int secondsPerCoin, LocalDate coinResetDate, LocalDate metricDate) {
        this.blocks = blocks;
        this.passes = passes;
        this.usageSeconds = usageSeconds;
        this.triggerCounts = triggerCounts;
        this.coins = coins;
        this.totalProductiveSeconds = totalProductiveSeconds;
        this.dailyProductiveSeconds = dailyProductiveSeconds;
        this.lifetimeProductiveSeconds = lifetimeProductiveSeconds;
        this.hasLifetimeProductiveSeconds = true;
        this.secondsPerCoin = secondsPerCoin;
        this.coinResetDate = coinResetDate;
        this.metricDate = metricDate;
    }

    public List<Block> getBlocks() { return blocks; }
    public List<Pass> getPasses() { return passes; }
    public Map<String, Long> getUsageSeconds() { return usageSeconds; }
    public Map<String, Integer> getTriggerCounts() { return triggerCounts; }
    public int getCoins() { return coins; }
    public double getTotalProductiveSeconds() { return totalProductiveSeconds; }
    public double getDailyProductiveSeconds() { return dailyProductiveSeconds; }
    public boolean hasLifetimeProductiveSeconds() { return hasLifetimeProductiveSeconds; }
    public double getLifetimeProductiveSeconds() { return lifetimeProductiveSeconds; }
    public int getSecondsPerCoin() { return secondsPerCoin; }
    public LocalDate getCoinResetDate() { return coinResetDate; }
    public LocalDate getMetricDate() { return metricDate; }
}
