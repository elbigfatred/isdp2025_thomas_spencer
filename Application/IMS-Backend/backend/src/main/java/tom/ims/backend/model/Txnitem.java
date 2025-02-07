package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "txnitems")
public class Txnitem {
    @EmbeddedId
    private TxnitemId id;

    @MapsId("txnID")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "txnID", nullable = false)
    private Txn txnID;

    @MapsId("itemID")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ItemID", nullable = false)
    private Item itemID;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "notes")
    private String notes;

    public void setId(TxnitemId id) {
        this.id = id;
    }

    public TxnitemId getId() {
        return id;
    }

    public Txn gettxnID(){
        return txnID;
    }

    public void setTxnID(Txn txnID){
        this.txnID = txnID;
    }

    public void settxnID(Txn txnID){
        this.txnID = txnID;
    }

    public Item getItemID(){
        return itemID;
    }

    public void setItemID(Item itemID){
        this.itemID = itemID;
    }

    public Integer getQuantity(){
        return quantity;
    }

    public void setQuantity(Integer quantity){
        this.quantity = quantity;
    }


    // ✅ Explicitly set the composite key
    public void setTxnAndItem(Txn txn, Item item) {
        this.txnID = txn;
        this.itemID = item;

        this.id = new TxnitemId();
        this.id.setTxnID(txn.getId());
        this.id.setItemID(item.getId());
    }

}