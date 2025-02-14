package tom.ims.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Txnaudit;
import tom.ims.backend.repository.TxnauditRepository;

import java.time.LocalDateTime;

@Service
public class TxnauditService {

    @Autowired
    private TxnauditRepository txnauditRepository;

    public void createAuditEntry(Txn txn, Employee employee, String actionDetails) {
        Txnaudit auditEntry = new Txnaudit();
        auditEntry.setTxnID(txn.getId());
        auditEntry.setEmployee(employee);
        auditEntry.setTxnType(txn.getTxnType().getTxnType());
        auditEntry.setStatus(txn.getTxnStatus().getStatusName());
        auditEntry.setTxnDate(LocalDateTime.now());
        auditEntry.setSite(txn.getSiteIDTo());
        auditEntry.setDelivery(txn.getDeliveryID());
        auditEntry.setNotes(actionDetails);

        txnauditRepository.save(auditEntry);
    }
}