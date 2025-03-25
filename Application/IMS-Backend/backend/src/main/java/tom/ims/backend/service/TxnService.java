package tom.ims.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Txnstatus;
import tom.ims.backend.repository.TxnRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    public Txn createLossReturnTxn(Txn txn) {
        // Set the created date
        txn.setCreatedDate(LocalDateTime.now());

        // Set default status to "SUBMITTED" if not already set
        if (txn.getTxnStatus() == null) {
            Txnstatus defaultStatus = new Txnstatus();
            defaultStatus.setStatusName("SUBMITTED"); // Make sure backend maps this correctly
            txn.setTxnStatus(defaultStatus);
        }

        // Barcodes, deliveryID, etc. are not relevant for Loss/Return/Damage
        txn.setBarCode(generateLossReturnBarcode());
        txn.setDeliveryID(null);
        txn.setEmergencyDelivery(null);
        txn.setShipDate(LocalDateTime.now());

        return txnRepository.save(txn);
    }

    private String generateLossReturnBarcode() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dateFormatter);
        int randomSuffix = new Random().nextInt(9000) + 1000; // 4-digit suffix
        return "LR-" + datePart + "-" + randomSuffix;
    }
}