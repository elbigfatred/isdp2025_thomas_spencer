package tom.ims.backend.model;

import java.math.BigDecimal;

public class DeliveryTxnDTO {
    private Txn txn;
    private int totalItems;
    private BigDecimal totalWeight;

    public DeliveryTxnDTO(Txn txn, int totalItems, BigDecimal totalWeight) {
        this.txn = txn;
        this.totalItems = totalItems;
        this.totalWeight = totalWeight;
    }

    public Txn getTxn() {
        return txn;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }
}