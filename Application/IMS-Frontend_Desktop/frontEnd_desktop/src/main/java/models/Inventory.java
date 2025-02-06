package models;

public class Inventory {
    private int itemID;
    private int siteID;
    private String itemLocation;
    private Item item;
    private int quantity;
    private int reorderThreshold;
    private int optimumThreshold;
    private String notes;

    // ✅ Constructor
    public Inventory(int itemID, int siteID, Item item,
                     int quantity, int reorderThreshold, int optimumThreshold, String notes, String itemLocation) {
        this.itemID = itemID;
        this.siteID = siteID;
        this.item = item;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
        this.optimumThreshold = optimumThreshold;
        this.notes = notes;
        this.itemLocation = itemLocation;
    }

    // ✅ Empty constructor for JSON parsing
    public Inventory() {}

    // 🔹 Getters
    public int getItemID() {
        return itemID;
    }

    public int getSiteID() {
        return siteID;
    }

    public Item getItem() {
        return item;
    }

    public String getItemLocation() {
        return itemLocation;
    }


    public int getQuantity() {
        return quantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public int getOptimumThreshold() {
        return optimumThreshold;
    }

    public String getNotes() {
        return notes;
    }

    // 🔹 Setters
    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public void setSiteID(int siteID) {
        this.siteID = siteID;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public void setOptimumThreshold(int optimumThreshold) {
        this.optimumThreshold = optimumThreshold;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "itemID=" + itemID +
                ", siteID=" + siteID +
                ", item=" + item +
                ", quantity=" + quantity +
                ", reorderThreshold=" + reorderThreshold +
                ", optimumThreshold=" + optimumThreshold +
                ", notes='" + notes + '\'' +
                ", itemLocation='" + itemLocation + '\'' +
                '}';
    }
}