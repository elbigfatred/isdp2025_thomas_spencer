package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Txnaudit;

import java.util.List;

@Repository
public interface TxnauditRepository extends JpaRepository<Txnaudit, Integer> {

    // ✅ Retrieve all audits for a specific transaction
    List<Txnaudit> findByTxnID(Txn txn);

    // ✅ Retrieve all audits related to a specific employee
    List<Txnaudit> findByEmployee_Id(Integer employeeID); // ✅ Corrected method

    // ✅ Retrieve all audits for a specific transaction type
    List<Txnaudit> findByTxnType(String txnType);

    // ✅ Retrieve the most recent audit entries (e.g., last 10 changes)
    List<Txnaudit> findTop10ByOrderByTxnDateDesc();
}