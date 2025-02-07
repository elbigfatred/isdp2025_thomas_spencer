package tom.ims.backend.model;

import java.util.List;

public class OrderUpdateRequest {
    private Integer txnID;
    private List<OrderItemUpdate> items;

    // Explicit Getter for txnID
    public Integer getTxnID() {
        return txnID;
    }

    // Explicit Setter for txnID
    public void setTxnID(Integer txnID) {
        this.txnID = txnID;
    }

    // Explicit Getter for items list
    public List<OrderItemUpdate> getItems() {
        return items;
    }

    // Explicit Setter for items list
    public void setItems(List<OrderItemUpdate> items) {
        this.items = items;
    }

    // ✅ Nested static class for individual item updates
    public static class OrderItemUpdate {
        private Integer itemID;
        private Integer quantity;

        // Explicit Getter for itemID
        public Integer getItemID() {
            return itemID;
        }

        // Explicit Setter for itemID
        public void setItemID(Integer itemID) {
            this.itemID = itemID;
        }

        // Explicit Getter for quantity
        public Integer getQuantity() {
            return quantity;
        }

        // Explicit Setter for quantity
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}