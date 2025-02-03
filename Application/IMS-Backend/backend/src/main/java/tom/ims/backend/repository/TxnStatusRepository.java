package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.Item;
import tom.ims.backend.model.Txnstatus;

import java.util.Optional;

public interface TxnStatusRepository extends JpaRepository<Txnstatus, Integer> {
    Optional<Txnstatus> findBystatusName(String statusName);
}
