package models;

public class TxnType {
    private String txnType;
    private boolean active;

    // ✅ Constructor
    public TxnType(String txnType, boolean active) {
        this.txnType = txnType;
        this.active = active;
    }

    // ✅ Empty constructor for JSON parsing
    public TxnType() {}

    // 🔹 Getters
    public String getTxnType() {
        return txnType;
    }

    public boolean isActive() {
        return active;
    }

    // 🔹 Setters
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "TxnType{" +
                "txnType='" + txnType + '\'' +
                ", active=" + active +
                '}';
    }
}