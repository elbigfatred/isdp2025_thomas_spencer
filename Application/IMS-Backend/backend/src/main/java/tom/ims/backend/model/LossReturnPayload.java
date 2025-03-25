package tom.ims.backend.model;

import java.util.List;

public class LossReturnPayload {
    private Txn txn;
    private List<Txnitem> txnItems;

    // Getters and setters
    // Getter and Setter for txn
    public Txn getTxn() {
        return txn;
    }

    public void setTxn(Txn txn) {
        this.txn = txn;
    }

    // Getter and Setter for txnItems
    public List<Txnitem> getTxnItems() {
        return txnItems;
    }

    public void setTxnItems(List<Txnitem> txnItems) {
        this.txnItems = txnItems;
    }
}
