import java.io.*;

public class SaveManager {
    private static final String FILE_NAME = "citrus_save.dat";

    public static void save(BlockManager blockManager, UserEconomy economy) {
        SaveData data = new SaveData(
                blockManager.getBlocks(), blockManager.getPasses(), blockManager.getUsageSeconds(), blockManager.getTriggerCounts(),
                economy.getCoins(), economy.getTotalProductiveSecondsRaw(), economy.getDailyProductiveSeconds(),
                economy.getLifetimeProductiveSeconds(), economy.getSecondsPerCoin(), economy.getCoinResetDate(), blockManager.getMetricDate(), economy.getDailyGoalHours());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(data);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void load(BlockManager blockManager, UserEconomy economy) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SaveData data = (SaveData) in.readObject();
            if (data.getBlocks() != null) for (Block block : data.getBlocks()) blockManager.addBlock(block);
            blockManager.setPasses(data.getPasses());
            blockManager.setUsageSeconds(data.getUsageSeconds());
            blockManager.setTriggerCounts(data.getTriggerCounts());
            blockManager.restoreMetricDate(data.getMetricDate());
            economy.setCoins(data.getCoins());
            economy.setCoinResetDate(data.getCoinResetDate());
            economy.resetCoinsIfNewDay();
            boolean currentMetricDay = economy.restoreDailyMetricDate(data.getMetricDate());
            if (currentMetricDay) {
                economy.setTotalProductiveSecondsRaw(data.getTotalProductiveSeconds());
                economy.setDailyProductiveSeconds(data.getDailyProductiveSeconds());
            }
            economy.setSecondsPerCoin(data.getSecondsPerCoin());
            economy.setDailyGoalHours(data.getDailyGoalHours());
            if (data.hasLifetimeProductiveSeconds()) economy.setLifetimeProductiveSeconds(data.getLifetimeProductiveSeconds());
            else economy.setLifetimeProductiveSeconds(data.getCoins() * (double) data.getSecondsPerCoin() + data.getTotalProductiveSeconds());
        } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
    }
}
