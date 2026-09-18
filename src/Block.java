import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Represents a website or app block rule
public class Block implements Serializable {
    private String targetName;
    private LockType lockType;
    private boolean active;
    private static final long serialVersionUID = 1L;
    private int timesTriggered = 0;

    private LocalDateTime unlockAt;
    private LocalTime rangeStart;
    private LocalTime rangeEnd;
    private Set<DayOfWeek> activeDays;
    private Map<DayOfWeek, DaySchedule> dailySchedules;
    private int challengeLength;
    private int delaySeconds;
    private LocalTime bedStart;
    private LocalTime bedEnd;
    private String emergencyPassword;

    public Block(String targetName, LockType lockType) {
        this.targetName = targetName;
        this.lockType = lockType;
        this.active = true;
    }

    public synchronized int getTimesTriggered() { return timesTriggered; }
    public synchronized void incrementTriggerCount() { timesTriggered++; }
    public String getTargetName() { return targetName; }
    public LockType getLockType() { return lockType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getUnlockAt() { return unlockAt; }
    public void setUnlockAt(LocalDateTime unlockAt) { this.unlockAt = unlockAt; }
    public boolean isTimerExpired() {
        return lockType == LockType.TIMER && unlockAt != null && !LocalDateTime.now().isBefore(unlockAt);
    }
    public LocalTime getRangeStart() { return rangeStart; }
    public void setRangeStart(LocalTime rangeStart) { this.rangeStart = rangeStart; }
    public LocalTime getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(LocalTime rangeEnd) { this.rangeEnd = rangeEnd; }
    public Set<DayOfWeek> getActiveDays() {
        return activeDays == null ? null : activeDays.isEmpty() ? EnumSet.noneOf(DayOfWeek.class) : EnumSet.copyOf(activeDays);
    }
    public void setActiveDays(Set<DayOfWeek> activeDays) {
        this.activeDays = activeDays == null || activeDays.isEmpty() ? null : EnumSet.copyOf(activeDays);
    }
    public Map<DayOfWeek, DaySchedule> getDailySchedules() {
        return dailySchedules == null ? new HashMap<>() : new HashMap<>(dailySchedules);
    }
    public void setDailySchedules(Map<DayOfWeek, DaySchedule> dailySchedules) {
        this.dailySchedules = dailySchedules == null || dailySchedules.isEmpty() ? null : new HashMap<>(dailySchedules);
    }
    public int getChallengeLength() { return challengeLength; }
    public void setChallengeLength(int challengeLength) { this.challengeLength = challengeLength; }
    public int getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(int delaySeconds) { this.delaySeconds = delaySeconds; }
    public LocalTime getBedStart() { return bedStart; }
    public void setBedStart(LocalTime bedStart) { this.bedStart = bedStart; }
    public LocalTime getBedEnd() { return bedEnd; }
    public void setBedEnd(LocalTime bedEnd) { this.bedEnd = bedEnd; }
    public String getEmergencyPassword() { return emergencyPassword; }
    public void setEmergencyPassword(String emergencyPassword) { this.emergencyPassword = emergencyPassword; }

    public boolean isCurrentlyBlocking() {
        if (!active) return false;
        LocalDateTime dateTimeNow = LocalDateTime.now();
        switch (lockType) {
            case TIMER:
                return unlockAt != null && dateTimeNow.isBefore(unlockAt);
            case TIME_RANGE:
                return isWithinSchedule(dateTimeNow);
            case BEDTIME:
                return isWithinRange(dateTimeNow.toLocalTime(), bedStart, bedEnd);
            case RANDOM_TEXT, DELAY, EMERGENCY, PASS_BLOCK:
                return true;
            default:
                return false;
        }
    }

    private boolean isWithinSchedule(LocalDateTime now) {
        if (dailySchedules != null && !dailySchedules.isEmpty()) {
            DaySchedule schedule = dailySchedules.get(now.getDayOfWeek());
            if (schedule != null && isWithinRange(now.toLocalTime(), schedule.start, schedule.end)) return true;
            DayOfWeek previous = now.getDayOfWeek().minus(1);
            DaySchedule overnight = dailySchedules.get(previous);
            return overnight != null && overnight.start.isAfter(overnight.end)
                    && now.toLocalTime().isBefore(overnight.end);
        }
        if (rangeStart == null || rangeEnd == null) return false;
        DayOfWeek scheduleDay = now.getDayOfWeek();
        if (rangeStart.isAfter(rangeEnd) && now.toLocalTime().isBefore(rangeEnd)) scheduleDay = scheduleDay.minus(1);
        return (activeDays == null || activeDays.contains(scheduleDay))
                && isWithinRange(now.toLocalTime(), rangeStart, rangeEnd);
    }

    private boolean isWithinRange(LocalTime now, LocalTime start, LocalTime end) {
        if (start == null || end == null || start.equals(end)) return false;
        return start.isBefore(end) ? !now.isBefore(start) && now.isBefore(end)
                : !now.isBefore(start) || now.isBefore(end);
    }

    public boolean validateRandomTextInput(String input) {
        if (input == null) return false;
        String[] words = input.trim().split("\\s+");
        if (words.length != challengeLength) return false;
        Set<String> seenWords = new HashSet<>();
        for (String word : words) {
            if (word.length() < 2 || !seenWords.add(word.toLowerCase())) return false;
        }
        return true;
    }

    public static class DaySchedule implements Serializable {
        private static final long serialVersionUID = 1L;
        private final LocalTime start;
        private final LocalTime end;
        public DaySchedule(LocalTime start, LocalTime end) { this.start = start; this.end = end; }
        public LocalTime getStart() { return start; }
        public LocalTime getEnd() { return end; }
    }
}
