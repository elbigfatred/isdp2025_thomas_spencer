package models;

public class TxnItem {
    private int txnID;
    private int itemID;
    private String itemName;
    private String itemSku;
    private int quantity;
    private String notes;

    // ✅ Constructor
    public TxnItem() {}

    public TxnItem(int txnID, int itemID, String itemName, String itemSku, int quantity, String notes) {
        this.txnID = txnID;
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemSku = itemSku;
        this.quantity = quantity;
        this.notes = notes;
    }

    // 🔹 Getters
    public int getTxnID() {
        return txnID;
    }

    public int getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemSku() {
        return itemSku;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    // 🔹 Setters
    public void setTxnID(int txnID) {
        this.txnID = txnID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemSku(String itemSku) {
        this.itemSku = itemSku;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "TxnItem{" +
                "txnID=" + txnID +
                ", itemID=" + itemID +
                ", itemName='" + itemName + '\'' +
                ", itemSku='" + itemSku + '\'' +
                ", quantity=" + quantity +
                ", notes='" + notes + '\'' +
                '}';
    }
}