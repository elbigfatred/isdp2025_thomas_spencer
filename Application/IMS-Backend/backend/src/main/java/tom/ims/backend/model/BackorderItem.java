package tom.ims.backend.model;

public class BackorderItem {
    private Integer itemID;
    private Integer quantity;

    // ✅ Constructor
    public BackorderItem(Integer itemID, Integer quantity) {
        this.itemID = itemID;
        this.quantity = quantity;
    }

    // ✅ GETTERS
    public Integer getItemID() { return itemID; }
    public Integer getQuantity() { return quantity; }

    // ✅ SETTERS
    public void setItemID(Integer itemID) { this.itemID = itemID; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}