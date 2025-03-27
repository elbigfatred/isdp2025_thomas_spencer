package tom.ims.backend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Site;
import tom.ims.backend.model.Txn;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TxnRepository extends JpaRepository<Txn, Integer> {

    List<Txn> findAll();

    // ✅ Fetch active orders for a given site
    @Query("SELECT t FROM Txn t WHERE t.siteIDTo.id = ?1 AND t.txnType.txnType = 'Store Order' AND t.txnStatus.statusName IN ('NEW','SUBMITTED','RECEIVED','ASSEMBLED','ASSEMBLING','IN TRANSIT','DELIVERED')")
    List<Txn> findActiveOrdersBySite(Integer siteId);

    // ✅ Fetch all transactions (store orders) for a given site
    List<Txn> findBySiteIDTo_Id(Integer siteId);

    // ✅ Fetch all orders with a specific status
    List<Txn> findByTxnStatus_StatusName(String status);

    @Query("SELECT t FROM Txn t WHERE t.siteIDTo.id = ?1 AND t.txnType.txnType = 'Emergency Order' AND t.txnStatus.statusName IN ('NEW','SUBMITTED','RECEIVED','ASSEMBLED','ASSEMBLING','IN TRANSIT','DELIVERED')")
    List<Txn> findActiveEmergencyOrdersBySite(Integer siteId);

    @Query("SELECT t FROM Txn t WHERE t.txnType.txnType IN ('Store Order', 'Emergency Order')")
    List<Txn> findAllStoreAndEmergencyOrders();

    @Query("SELECT t FROM Txn t WHERE t.siteIDTo.id = :siteID AND t.txnType.txnType = 'Back Order' AND t.txnStatus.statusName = 'NEW'")
    Txn findNewBackorderBySite(@Param("siteID") Integer siteID);

    @Query("SELECT t FROM Txn t " +
            "WHERE (t.txnType.txnType = 'Store Order' OR t.txnType.txnType = 'Emergency Order') " +
            "AND (t.txnStatus.statusName in ('ASSEMBLED','ASSEMBLING','RECEIVED') " +
            "OR t.deliveryID IS NOT NULL) " +
            "ORDER BY t.shipDate")
    List<Txn> findAllAssembledStoreOrders();

    Optional<Txn> findByIdAndTxnType_TxnType(Integer txnID, String typeName);

    @Query("SELECT t FROM Txn t WHERE t.txnType.txnType = 'Online' AND t.notes LIKE CONCAT('%\"email\":\"', :emailinput, '\"%')")
    List<Txn> findByCustomerEmail(@Param("emailinput") String emailinput);

    @Query("SELECT t FROM Txn t WHERE t.txnType.txnType = 'Online' AND t.siteIDTo.id = :siteID")
    List<Txn> findOnlineOrdersBySite(@Param("siteID") Integer siteID);

    // Repository Query
    @Query("SELECT t FROM Txn t WHERE t.txnType.txnType IN ('STORE ORDER', 'EMERGENCY ORDER') " +
            "AND t.deliveryID IS NOT NULL " +
            "AND DATE(t.shipDate) = :today")
    List<Txn> findByTxnTypeAndDeliveryAssignedToday(@Param("today") LocalDate today);

    //  Fetch active supplier order
    @Query("SELECT t FROM Txn t WHERE t.txnType.txnType = 'Supplier Order' AND t.txnStatus.statusName IN ('NEW')")
    List<Txn> findActiveSupplierOrders();
}