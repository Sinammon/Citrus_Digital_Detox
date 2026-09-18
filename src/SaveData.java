import java.io.Serializable;
import java.util.List;

public class SaveData implements Serializable {
    private List<Block> blocks;
    private int coins;
    private double totalProductiveSeconds;
    private double dailyProductiveSeconds;
    private double lifetimeProductiveSeconds;
    private boolean hasLifetimeProductiveSeconds;
    private int secondsPerCoin;
    private static final long serialVersionUID = 1L;

    public SaveData(List<Block> blocks, int coins, double totalProductiveSeconds,
                    double dailyProductiveSeconds, double lifetimeProductiveSeconds, int secondsPerCoin) {
        this.blocks = blocks;
        this.coins = coins;
        this.totalProductiveSeconds = totalProductiveSeconds;
        this.dailyProductiveSeconds = dailyProductiveSeconds;
        this.lifetimeProductiveSeconds = lifetimeProductiveSeconds;
        this.hasLifetimeProductiveSeconds = true;
        this.secondsPerCoin = secondsPerCoin;
    }
    public int getSecondsPerCoin() { return secondsPerCoin; }

    public List<Block> getBlocks() { return blocks; }
    public int getCoins() { return coins; }
    public double getTotalProductiveSeconds() { return totalProductiveSeconds; }
    public double getDailyProductiveSeconds() { return dailyProductiveSeconds; }
    public boolean hasLifetimeProductiveSeconds() { return hasLifetimeProductiveSeconds; }
    public double getLifetimeProductiveSeconds() { return lifetimeProductiveSeconds; }
}
