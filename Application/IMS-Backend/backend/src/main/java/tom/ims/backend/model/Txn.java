package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "txn")
public class Txn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txnID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employeeID", nullable = false)
    private Employee employeeID;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "siteIDTo", nullable = false)
    private Site siteIDTo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "siteIDFrom", nullable = false)
    private Site siteIDFrom;

    @Column(name = "shipDate", nullable = false)
    private LocalDateTime shipDate;

    @Column(name = "barCode", nullable = false, length = 50)
    private String barCode;

    @ColumnDefault("current_timestamp()")
    @Column(name = "createdDate", nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deliveryID")
    private Delivery deliveryID;

    @Column(name = "emergencyDelivery")
    private Byte emergencyDelivery;

    @Column(name = "notes")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "txnType")
    @JsonManagedReference
    private Txntype txnType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "txnStatus")
    @JsonManagedReference
    private Txnstatus txnStatus;

    // ✅ GETTERS
    public Integer getId() {
        return id;
    }

    public Employee getEmployeeID() {
        return employeeID;
    }

    public Site getSiteIDTo() {
        return siteIDTo;
    }

    public Site getSiteIDFrom() {
        return siteIDFrom;
    }

    public LocalDateTime getShipDate() {
        return shipDate;
    }

    public String getBarCode() {
        return barCode;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public Delivery getDeliveryID() {
        return deliveryID;
    }

    public Byte getEmergencyDelivery() {
        return emergencyDelivery;
    }

    public String getNotes() {
        return notes;
    }

    public Txntype getTxnType() {
        return txnType;
    }

    public Txnstatus getTxnStatus() {
        return txnStatus;
    }

    // ✅ SETTERS
    public void setId(Integer id) {
        this.id = id;
    }

    public void setEmployeeID(Employee employeeID) {
        this.employeeID = employeeID;
    }

    public void setSiteIDTo(Site siteIDTo) {
        this.siteIDTo = siteIDTo;
    }

    public void setSiteIDFrom(Site siteIDFrom) {
        this.siteIDFrom = siteIDFrom;
    }

    public void setShipDate(LocalDateTime shipDate) {
        this.shipDate = shipDate;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setDeliveryID(Delivery deliveryID) {
        this.deliveryID = deliveryID;
    }

    public void setEmergencyDelivery(Byte emergencyDelivery) {
        this.emergencyDelivery = emergencyDelivery;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTxnType(Txntype txnType) {
        this.txnType = txnType;
    }

    public void setTxnStatus(Txnstatus txnStatus) {
        this.txnStatus = txnStatus;
    }
}