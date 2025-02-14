package models;

import java.time.LocalDateTime;

public class Txn {
    private int id;
    private Employee employee;
    private Site siteTo;
    private Site siteFrom;
    private TxnStatus txnStatus;
    private LocalDateTime shipDate;
    private TxnType txnType;
    private String barCode;
    private LocalDateTime createdDate;
    private Integer deliveryID;
    private boolean emergencyDelivery;
    private String notes;

    // ✅ Constructor
    public Txn(int id, Employee employee, Site siteTo, Site siteFrom, TxnStatus txnStatus,
               LocalDateTime shipDate, TxnType txnType, String barCode, LocalDateTime createdDate,
               Integer deliveryID, boolean emergencyDelivery, String notes) {
        this.id = id;
        this.employee = employee;
        this.siteTo = siteTo;
        this.siteFrom = siteFrom;
        this.txnStatus = txnStatus;
        this.shipDate = shipDate;
        this.txnType = txnType;
        this.barCode = barCode;
        this.createdDate = createdDate;
        this.deliveryID = deliveryID;
        this.emergencyDelivery = emergencyDelivery;
        this.notes = notes;
    }

    // ✅ Empty constructor for JSON parsing
    public Txn() {}

    // 🔹 Getters
    public int getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Site getSiteTo() {
        return siteTo;
    }

    public Site getSiteFrom() {
        return siteFrom;
    }

    public TxnStatus getTxnStatus() {
        return txnStatus;
    }

    public LocalDateTime getShipDate() {
        return shipDate;
    }

    public TxnType getTxnType() {
        return txnType;
    }

    public String getBarCode() {
        return barCode;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Integer getDeliveryID() {
        return deliveryID;
    }

    public boolean isEmergencyDelivery() {
        return emergencyDelivery;
    }

    public String getNotes() {
        return notes;
    }

    // 🔹 Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setSiteTo(Site siteTo) {
        this.siteTo = siteTo;
    }

    public void setSiteFrom(Site siteFrom) {
        this.siteFrom = siteFrom;
    }

    public void setTxnStatus(TxnStatus txnStatus) {
        this.txnStatus = txnStatus;
    }

    public void setShipDate(LocalDateTime shipDate) {
        this.shipDate = shipDate;
    }

    public void setTxnType(TxnType txnType) {
        this.txnType = txnType;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setDeliveryID(Integer deliveryID) {
        this.deliveryID = deliveryID;
    }

    public void setEmergencyDelivery(boolean emergencyDelivery) {
        this.emergencyDelivery = emergencyDelivery;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Txn{" +
                "id=" + id +
                ", employee=" + employee +
                ", siteTo=" + siteTo +
                ", siteFrom=" + siteFrom +
                ", txnStatus=" + txnStatus +
                ", shipDate=" + shipDate +
                ", txnType=" + txnType +
                ", barCode='" + barCode + '\'' +
                ", createdDate=" + createdDate +
                ", deliveryID=" + deliveryID +
                ", emergencyDelivery=" + emergencyDelivery +
                ", notes='" + notes + '\'' +
                '}';
    }
}