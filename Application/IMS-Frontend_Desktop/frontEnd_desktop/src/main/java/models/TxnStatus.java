package models;

public class TxnStatus {
    private String statusName;
    private String statusDescription;
    private boolean active;

    // ✅ Constructor
    public TxnStatus(String statusName, String statusDescription, boolean active) {
        this.statusName = statusName;
        this.statusDescription = statusDescription;
        this.active = active;
    }

    // ✅ Empty constructor for JSON parsing
    public TxnStatus() {}

    // 🔹 Getters
    public String getStatusName() {
        return statusName;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public boolean isActive() {
        return active;
    }

    // 🔹 Setters
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return statusName;
    }
}