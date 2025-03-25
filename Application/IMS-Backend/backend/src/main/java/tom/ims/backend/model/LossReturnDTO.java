package tom.ims.backend.model;

public class LossReturnDTO {
    private int siteId;
    private int employeeId;
    private String txnType;
    private String notes;

    private int itemId;
    private int quantity;
    private String itemNotes;
    private boolean resellable;

    // --- Site ID ---
    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    // --- Employee ID ---
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    // --- Transaction Type ---
    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    // --- Notes ---
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // --- Item ID ---
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    // --- Quantity ---
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // --- Item Notes ---
    public String getItemNotes() {
        return itemNotes;
    }

    public void setItemNotes(String itemNotes) {
        this.itemNotes = itemNotes;
    }

    // --- Resellable ---
    public boolean isResellable() {
        return resellable;
    }

    public void setResellable(boolean resellable) {
        this.resellable = resellable;
    }
}