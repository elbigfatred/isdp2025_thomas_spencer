package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tom.ims.backend.model.Txntype;

import java.util.Optional;

public interface TxnTypeRepository extends JpaRepository<Txntype, Integer> {
    Optional<Txntype> getBytxnType(String txnType);

    // ✅ Find a transaction type by name (e.g., "Back Order")
    @Query("SELECT t FROM Txntype t WHERE t.txnType = :typeName")
    Txntype findByTypeName(@Param("typeName") String typeName);
}
