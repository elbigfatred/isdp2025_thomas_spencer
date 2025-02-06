package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Txnitem;
import tom.ims.backend.model.TxnitemId;

import java.util.List;

@Repository
public interface TxnItemsRepository extends JpaRepository<Txnitem, Integer> {
}