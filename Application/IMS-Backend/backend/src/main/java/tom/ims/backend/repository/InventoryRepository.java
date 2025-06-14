package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tom.ims.backend.model.Inventory;
import tom.ims.backend.model.InventoryId;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {

    // find all inventory items that need reordering at a specific site
    @Query("SELECT i FROM Inventory i WHERE i.id.siteID = :siteId AND i.quantity < i.optimumThreshold")
    List<Inventory> findLowStockItems(@Param("siteId") Integer siteId);

    List<Inventory> findById_SiteID(Integer siteID);

    List<Inventory> findById_ItemID(Integer itemID);

    List<Inventory> findById_SiteIDAndId_ItemID(Integer siteID, Integer itemID);

    // ✅ Use `id.siteId` and `id.itemId` when dealing with composite keys
    @Query("SELECT i FROM Inventory i WHERE i.id.siteID = :siteId AND i.id.itemID = :itemId")
    Inventory findBySiteIdAndItemId(@Param("siteId") Integer siteId, @Param("itemId") Integer itemId);
}