package tom.ims.backend.model;

public class OrderItem {
    private Integer itemID;
    private Integer siteID;
    private int quantity;
    private int reorderThreshold;
    private int optimumThreshold;
    private int orderQuantity;

    // ✅ Constructor
    public OrderItem(Integer itemID, Integer siteID, int quantity, int reorderThreshold, int optimumThreshold, int orderQuantity) {
        this.itemID = itemID;
        this.siteID = siteID;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
        this.optimumThreshold = optimumThreshold;
        this.orderQuantity = orderQuantity;
    }

    // ✅ GETTERS
    public Integer getItemID() {
        return itemID;
    }

    public Integer getSiteID() {
        return siteID;
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

    public int getOrderQuantity() {
        return orderQuantity;
    }

    // ✅ SETTERS
    public void setItemID(Integer itemID) {
        this.itemID = itemID;
    }

    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public void setOptimumThreshold(int optimumThreshold) {
        this.optimumThreshold = optimumThreshold;
    }

    public void setOrderQuantity(int orderQuantity) {
        this.orderQuantity = orderQuantity;
    }
}