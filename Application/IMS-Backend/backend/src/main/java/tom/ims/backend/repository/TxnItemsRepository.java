package tom.ims.backend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Item;
import tom.ims.backend.model.Txn;
import tom.ims.backend.model.Txnitem;
import tom.ims.backend.model.TxnitemId;

import java.util.List;

@Repository
public interface TxnItemsRepository extends JpaRepository<Txnitem, Integer> {

    // ✅ Fetch all items for a given txnID
    List<Txnitem> findById_TxnID(Integer txnId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Txnitem t WHERE t.txnID = :txn")
    void deleteByTxnID(@Param("txn") Txn txn);

    @Query("SELECT t FROM Txnitem t WHERE t.txnID = :txn AND t.itemID = :item")
    Txnitem findByTxnIDAndItemID(@Param("txn") Txn txn, @Param("item") Item item);
}