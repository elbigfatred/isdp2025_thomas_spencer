package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Site;
import tom.ims.backend.model.Txn;

import java.util.List;
import java.util.Optional;

@Repository
public interface TxnRepository extends JpaRepository<Txn, Integer> {
    // ✅ Fetch active orders for a given site
    @Query("SELECT t FROM Txn t WHERE t.siteIDTo.id = ?1 AND t.txnType.txnType = 'Store Order' AND t.txnStatus.statusName IN ('NEW', 'SUBMITTED')")
    List<Txn> findActiveOrdersBySite(Integer siteId);
}