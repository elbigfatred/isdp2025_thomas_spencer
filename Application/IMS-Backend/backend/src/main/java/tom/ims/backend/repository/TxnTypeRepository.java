package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Txntype;

import java.util.Optional;

public interface TxnTypeRepository extends JpaRepository<Txntype, Integer> {
    Optional<Txntype> getBytxnType(String txnType);
}
