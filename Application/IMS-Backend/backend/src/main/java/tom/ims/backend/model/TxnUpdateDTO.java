package tom.ims.backend.model;

import java.time.LocalDateTime;

public class TxnUpdateDTO {
    private Integer siteIDTo;
    private String txnStatus;
    private LocalDateTime shipDate;  // ✅ Updated to LocalDateTime
    private String txnType;
    private String barCode;
    private Integer deliveryID;
    private Byte emergencyDelivery;  // ✅ Updated to Byte

    // ✅ Getters and Setters
    public Integer getSiteIDTo() {
        return siteIDTo;
    }

    public void setSiteIDTo(Integer siteIDTo) {
        this.siteIDTo = siteIDTo;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    public void setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public LocalDateTime getShipDate() {
        return shipDate;
    }

    public void setShipDate(LocalDateTime shipDate) {
        this.shipDate = shipDate;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Integer getDeliveryID() {
        return deliveryID;
    }

    public void setDeliveryID(Integer deliveryID) {
        this.deliveryID = deliveryID;
    }

    public Byte getEmergencyDelivery() {
        return emergencyDelivery;
    }

    public void setEmergencyDelivery(Byte emergencyDelivery) {
        this.emergencyDelivery = emergencyDelivery;
    }
}