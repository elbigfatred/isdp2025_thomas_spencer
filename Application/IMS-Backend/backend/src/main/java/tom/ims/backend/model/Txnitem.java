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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "txnID", nullable = false)
    private Txn txnID;

    @MapsId("itemID")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ItemID", nullable = false)
    private Item itemID;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "notes")
    private String notes;

}