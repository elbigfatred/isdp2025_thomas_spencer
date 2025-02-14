package tom.ims.backend.model;

import java.util.List;

public class InventoryUpdateRequest {
    private Integer siteID;
    private List<InventoryUpdateItem> items;

    public Integer getSiteID() {
        return siteID;
    }

    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }

    public List<InventoryUpdateItem> getItems() {
        return items;
    }

    public void setItems(List<InventoryUpdateItem> items) {
        this.items = items;
    }

    public static class InventoryUpdateItem {
        private Integer itemID;
        private Integer quantity;

        public Integer getItemID() {
            return itemID;
        }

        public void setItemID(Integer itemID) {
            this.itemID = itemID;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}