package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Inventory;
import tom.ims.backend.model.InventoryId;
import tom.ims.backend.model.OrderItem;
import tom.ims.backend.repository.InventoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // ✅ Fetch inventory items that need reordering for a site
    public List<Inventory> getItemsBelowThreshold(Integer siteId) {
        List<Inventory> inventoryItems = inventoryRepository.findLowStockItems(siteId);

        return inventoryItems;
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoryBySite(Integer siteID) {
        return inventoryRepository.findById_SiteID(siteID);
    }

    public Optional<Inventory> getInventoryById(InventoryId id) {
        return inventoryRepository.findById(id);
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(InventoryId id, Inventory updatedInventory) {
        return inventoryRepository.findById(id).map(existingInventory -> {
            existingInventory.setQuantity(updatedInventory.getQuantity());
            existingInventory.setReorderThreshold(updatedInventory.getReorderThreshold());
            existingInventory.setOptimumThreshold(updatedInventory.getOptimumThreshold());
            existingInventory.setNotes(updatedInventory.getNotes());
            return inventoryRepository.save(existingInventory);
        }).orElse(null);
    }

    public boolean deleteInventory(InventoryId id) {
        if (inventoryRepository.existsById(id)) {
            inventoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

}