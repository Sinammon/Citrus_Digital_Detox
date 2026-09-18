import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Block> blocks;
    private List<Pass> passes;
    private Map<String, Long> usageSeconds;
    private int coins;
    private double totalProductiveSeconds;
    private double dailyProductiveSeconds;
    private double lifetimeProductiveSeconds;
    private boolean hasLifetimeProductiveSeconds;
    private int secondsPerCoin;

    public SaveData(List<Block> blocks, List<Pass> passes, Map<String, Long> usageSeconds, int coins,
                    double totalProductiveSeconds, double dailyProductiveSeconds,
                    double lifetimeProductiveSeconds, int secondsPerCoin) {
        this.blocks = blocks;
        this.passes = passes;
        this.usageSeconds = usageSeconds;
        this.coins = coins;
        this.totalProductiveSeconds = totalProductiveSeconds;
        this.dailyProductiveSeconds = dailyProductiveSeconds;
        this.lifetimeProductiveSeconds = lifetimeProductiveSeconds;
        this.hasLifetimeProductiveSeconds = true;
        this.secondsPerCoin = secondsPerCoin;
    }

    public List<Block> getBlocks() { return blocks; }
    public List<Pass> getPasses() { return passes; }
    public Map<String, Long> getUsageSeconds() { return usageSeconds; }
    public int getCoins() { return coins; }
    public double getTotalProductiveSeconds() { return totalProductiveSeconds; }
    public double getDailyProductiveSeconds() { return dailyProductiveSeconds; }
    public boolean hasLifetimeProductiveSeconds() { return hasLifetimeProductiveSeconds; }
    public double getLifetimeProductiveSeconds() { return lifetimeProductiveSeconds; }
    public int getSecondsPerCoin() { return secondsPerCoin; }
}
