package tom.ims.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tom.ims.backend.model.Txn;
import tom.ims.backend.repository.TxnRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TxnService {
    private final TxnRepository txnRepository;

    public TxnService(TxnRepository txnRepository) {
        this.txnRepository = txnRepository;
    }

    public List<Txn> findAll(){
        return txnRepository.findAll();
    }

    // ✅ Fetch a specific transaction by ID
    public Optional<Txn> getTransactionById(Integer txnId) {
        return txnRepository.findById(txnId);
    }

    // ✅ Update a transaction
    @Transactional
    public Txn updateTransaction(Integer txnId, Txn updatedTxn) {
        return txnRepository.findById(txnId)
                .map(existingTxn -> {
                    existingTxn.setSiteIDTo(updatedTxn.getSiteIDTo());
                    existingTxn.setTxnStatus(updatedTxn.getTxnStatus());
                    existingTxn.setShipDate(updatedTxn.getShipDate());
                    existingTxn.setTxnType(updatedTxn.getTxnType());
                    existingTxn.setBarCode(updatedTxn.getBarCode());
                    existingTxn.setEmergencyDelivery(updatedTxn.getEmergencyDelivery());

                    return txnRepository.save(existingTxn);
                })
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}