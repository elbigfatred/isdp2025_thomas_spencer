package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.TxnUpdateDTO;
import tom.ims.backend.model.Txnstatus;
import tom.ims.backend.model.Txntype;
import tom.ims.backend.service.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/txns")
public class TxnController {
    private final TxnService txnService;
    @Autowired private SiteService siteService;
    @Autowired private TxnStatusService txnStatusService;
    @Autowired private TxnTypeService txnTypeService;
    @Autowired private DeliveryService deliveryService;

    public TxnController(TxnService txnService) {
        this.txnService = txnService;
    }

    //  Get all transactions (Admin access)
    @GetMapping
    public ResponseEntity<List<Txn>> getAllTransactions() {
        return ResponseEntity.ok(txnService.findAll());
    }

    //  Get all active transactions (Excludes "CLOSED")
    @GetMapping("/active")
    public ResponseEntity<List<Txn>> getAllActiveTransactions() {
        return ResponseEntity.ok(txnService.findAll().stream()
                .filter(txn -> !"CANCELLED".equalsIgnoreCase(txn.getTxnStatus().getStatusName()))
                .toList());
    }

    //  Get a transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<Txn> getTransactionById(@PathVariable Integer id) {
        Optional<Txn> txn = txnService.getTransactionById(id);
        return txn.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/txnTypes")
    public ResponseEntity<List<Txntype>> getAllTxnTypes() {
        List<Txntype> txnTypes = txnTypeService.getAll();
        return ResponseEntity.ok(txnTypes);
    }

    @GetMapping("/txnStatuses")
    public ResponseEntity<List<Txnstatus>> getAllTxnStatuses() {
        List<Txnstatus> txnStatuses = txnStatusService.findAll();
        return ResponseEntity.ok(txnStatuses);
    }

    //  Modify a transaction
    @PutMapping("/{id}")
    public ResponseEntity<Txn> updateTransaction(@PathVariable Integer id, @RequestBody TxnUpdateDTO txnUpdateDTO) {
        try {
            System.out.println("[DEBUG] Updating transaction ID: " + id);

            //  Retrieve the existing transaction
            Txn existingTxn = txnService.getTransactionById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            //  Fetch related objects using their respective services
            if (txnUpdateDTO.getSiteIDTo() != null) {
                existingTxn.setSiteIDTo(siteService.getSiteById(txnUpdateDTO.getSiteIDTo()));
            }
            if (txnUpdateDTO.getTxnStatus() != null) {
                existingTxn.setTxnStatus(txnStatusService.findByName(txnUpdateDTO.getTxnStatus()));
            }
            if (txnUpdateDTO.getTxnType() != null) {
                existingTxn.setTxnType(txnTypeService.getbyTxnType(txnUpdateDTO.getTxnType()));
            }
            if (txnUpdateDTO.getShipDate() != null) {
                existingTxn.setShipDate(txnUpdateDTO.getShipDate());
            }
            if (txnUpdateDTO.getBarCode() != null) {
                existingTxn.setBarCode(txnUpdateDTO.getBarCode());
            }
            if (txnUpdateDTO.getDeliveryID() != null) {
                existingTxn.setDeliveryID(deliveryService.getDeliveryById(txnUpdateDTO.getDeliveryID()));
            }
            else{
                existingTxn.setDeliveryID(null);
            }
            if (txnUpdateDTO.getEmergencyDelivery() != null) {
                existingTxn.setEmergencyDelivery(txnUpdateDTO.getEmergencyDelivery());
            }

            //  Save the updated transaction
            Txn updatedTxn = txnService.updateTransaction(id, existingTxn);
            return ResponseEntity.ok(updatedTxn);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update transaction: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}