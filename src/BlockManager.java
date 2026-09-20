import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockManager {
    private final List<Block> blocks = new CopyOnWriteArrayList<>();
    private final List<Pass> passes = new CopyOnWriteArrayList<>();
    private final Map<String, Long> usageSeconds = new LinkedHashMap<>();
    private final Map<String, Integer> triggerCounts = new LinkedHashMap<>();
    private LocalDate metricDate = LocalDate.now();

    public void addPass(Pass pass) { passes.add(pass); }
    public boolean removeCustomPass(Pass pass) { return pass != null && pass.isCustom() && passes.remove(pass); }
    public List<Pass> getPasses() { return passes; }
    public void setPasses(List<Pass> savedPasses) { passes.clear(); if (savedPasses != null) passes.addAll(savedPasses); }
    public List<Pass> getCustomPasses() { return passes.stream().filter(Pass::isCustom).toList(); }

    public synchronized void recordUsage(String targetName, double seconds) {
        resetIfDateChanged();
        if (targetName == null || targetName.isBlank() || seconds <= 0) return;
        usageSeconds.merge(targetName, Math.max(1, Math.round(seconds)), Long::sum);
    }
    public void recordUsageForWindow(String activeWindowTitle, double seconds) {
        if (activeWindowTitle == null || activeWindowTitle.isBlank()) return;
        recordUsage(activeWindowTitle.trim(), seconds);
    }
    public synchronized Map<String, Long> getUsageSeconds() { resetIfDateChanged(); return new LinkedHashMap<>(usageSeconds); }
    public synchronized void setUsageSeconds(Map<String, Long> savedUsage) { usageSeconds.clear(); if (savedUsage != null) usageSeconds.putAll(savedUsage); }

    public synchronized void recordTrigger(Block block) {
        resetIfDateChanged();
        block.incrementTriggerCount();
        triggerCounts.merge(block.getTargetName(), 1, Integer::sum);
    }
    public synchronized Map<String, Integer> getTriggerCounts() { resetIfDateChanged(); return new LinkedHashMap<>(triggerCounts); }
    public synchronized void setTriggerCounts(Map<String, Integer> savedCounts) { triggerCounts.clear(); if (savedCounts != null) triggerCounts.putAll(savedCounts); }
    public synchronized LocalDate getMetricDate() { return metricDate; }
    public synchronized void restoreMetricDate(LocalDate savedDate) {
        if (savedDate == null || !LocalDate.now().equals(savedDate)) { usageSeconds.clear(); triggerCounts.clear(); }
        metricDate = LocalDate.now();
    }
    public synchronized void resetDailyMetrics() { usageSeconds.clear(); triggerCounts.clear(); metricDate = LocalDate.now(); }
    private void resetIfDateChanged() { if (!LocalDate.now().equals(metricDate)) resetDailyMetrics(); }

    private boolean hasActivePass(String targetName) {
        passes.removeIf(pass -> pass.isPurchased() && !pass.isActive());
        return passes.stream().anyMatch(pass -> pass.isActive() && pass.getTargetName().equalsIgnoreCase(targetName));
    }
    public void addBlock(Block block) { blocks.add(block); }
    public void removeBlock(Block block) { blocks.remove(block); }
    public List<Block> getBlocks() { return blocks; }
    public int countActiveBlocks() { int count = 0; for (Block block : blocks) if (block.isCurrentlyBlocking()) count++; return count; }
    public boolean isBypassedWithPass(Block block) { return block != null && passes.stream().anyMatch(pass -> pass.isActive() && pass.getTargetName().equalsIgnoreCase(block.getTargetName())); }

    public Block findMatchingBlock(String activeWindowTitle) {
        if (activeWindowTitle == null || activeWindowTitle.isBlank()) return null;
        String lower = activeWindowTitle.toLowerCase();
        for (Block block : blocks) if (block.isCurrentlyBlocking() && lower.contains(block.getTargetName().toLowerCase()) && !hasActivePass(block.getTargetName())) return block;
        return null;
    }
}
