import java.io.*;

public class SaveManager {
    private static final String FILE_NAME = "citrus_save.dat";

    public static void save(BlockManager blockManager, UserEconomy economy) {
        SaveData data = new SaveData(
                blockManager.getBlocks(),
                economy.getCoins(),
                economy.getTotalProductiveSecondsRaw(),
                economy.getDailyProductiveSeconds(),
                economy.getLifetimeProductiveSeconds(),
                economy.getSecondsPerCoin());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(BlockManager blockManager, UserEconomy economy) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SaveData data = (SaveData) in.readObject();
            for (Block b : data.getBlocks()) {
                blockManager.addBlock(b);
            }
            economy.setCoins(data.getCoins());
            economy.setTotalProductiveSecondsRaw(data.getTotalProductiveSeconds());
            economy.setDailyProductiveSeconds(data.getDailyProductiveSeconds());
            economy.setSecondsPerCoin(data.getSecondsPerCoin());
            if (data.hasLifetimeProductiveSeconds()) {
                economy.setLifetimeProductiveSeconds(data.getLifetimeProductiveSeconds());
            } else {
                // Save files created before lifetime tracking can only be estimated.
                economy.setLifetimeProductiveSeconds(
                        data.getCoins() * (double) data.getSecondsPerCoin()
                                + data.getTotalProductiveSeconds());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
