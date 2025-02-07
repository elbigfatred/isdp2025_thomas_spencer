package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TxnitemId implements Serializable {
    private static final long serialVersionUID = -3493836387010733693L;

    @Column(name = "txnID", nullable = false)
    private Integer txnID;

    @Column(name = "ItemID", nullable = false)
    private Integer itemID;

    // ✅ Explicit Getter & Setter for ID
    public Integer getId() {
        return txnID;
    }

    public void setId(Integer id) {
        this.txnID = id;
    }

    // ✅ Explicit Getter for txnID
    public Integer getTxnID() {
        return txnID;
    }

    // ✅ Explicit Setter for txnID
    public void setTxnID(Integer txnID) {
        this.txnID = txnID;
    }

    // ✅ Explicit Getter for itemID
    public Integer getItemID() {
        return itemID;
    }

    // ✅ Explicit Setter for itemID
    public void setItemID(Integer itemID) {
        this.itemID = itemID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TxnitemId entity = (TxnitemId) o;
        return Objects.equals(this.itemID, entity.itemID) &&
                Objects.equals(this.txnID, entity.txnID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID, txnID);
    }
}