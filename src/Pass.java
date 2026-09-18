import java.io.Serializable;
import java.time.LocalDateTime;

public class Pass implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String targetName;
    private final String displayName;
    private final String description;
    private final int minutes;
    private final boolean custom;
    private LocalDateTime expiresAt;

    public Pass(String targetName, int minutes) {
        this(targetName, titleCase(targetName) + " Pass", "Temporary access to " + titleCase(targetName) + ".", minutes, false, LocalDateTime.now().plusMinutes(minutes));
    }

    public Pass(String displayName, String description, int minutes) {
        this(displayName.toLowerCase().replace(' ', '-'), displayName + " Pass", description, minutes, true, null);
    }

    public Pass(String targetName, String displayName, String description, int minutes) {
        this(targetName, displayName, description, minutes, false, null);
    }

    private Pass(String targetName, String displayName, String description, int minutes, boolean custom, LocalDateTime expiresAt) {
        this.targetName = targetName;
        this.displayName = displayName;
        this.description = description;
        this.minutes = minutes;
        this.custom = custom;
        this.expiresAt = expiresAt;
    }

    public String getTargetName() { return targetName; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getMinutes() { return minutes; }
    public boolean isCustom() { return custom; }
    public boolean isPurchased() { return expiresAt != null; }
    public boolean isActive() { return expiresAt != null && LocalDateTime.now().isBefore(expiresAt); }
    public void activate() { expiresAt = LocalDateTime.now().plusMinutes(minutes); }

    private static String titleCase(String value) {
        if (value == null || value.isBlank()) return "App";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
    }
}
