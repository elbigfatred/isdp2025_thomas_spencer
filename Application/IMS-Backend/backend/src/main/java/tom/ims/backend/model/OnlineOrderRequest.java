package tom.ims.backend.model;

import java.util.List;

public class OnlineOrderRequest {
    private CustomerInfo customer;
    private List<OrderItem> items; // ✅ Use OrderItem instead of Txnitem
    private Integer createdByUserID;
    private Integer siteID;

    // Getters and Setters
    public CustomerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Integer getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(Integer createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    public Integer getSiteID() {
        return siteID;
    }

    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }

    // Nested Class for CustomerInfo
    public static class CustomerInfo {
        private String name;
        private String phone;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // ✅ Keep OrderItem separate from Txnitem
    public static class OrderItem {
        private int itemID;
        private int quantity;

        public int getItemID() {
            return itemID;
        }

        public void setItemID(int itemID) {
            this.itemID = itemID;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}