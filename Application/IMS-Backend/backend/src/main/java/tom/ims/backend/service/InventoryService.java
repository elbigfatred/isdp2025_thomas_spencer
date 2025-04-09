package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final int warehouseID = 2;


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


    public List<Inventory> getInventoryBySiteAndItem(Integer siteID, Integer itemID) {
        System.out.println("[DEBUG] Looking up inventory in database for siteID: " + siteID + ", itemID: " + itemID);
        return inventoryRepository.findById_SiteIDAndId_ItemID(siteID, itemID);
    }


    public void decrementInventory(InventoryUpdateRequest request) {
        for (InventoryUpdateRequest.InventoryUpdateItem item : request.getItems()) {
            Inventory inventory = inventoryRepository.findBySiteIdAndItemId(request.getSiteID(), item.getItemID());
            if (inventory != null) {
                inventory.setQuantity(Math.max(0, inventory.getQuantity() - item.getQuantity()));
                inventoryRepository.save(inventory);
            }
        }
    }


    public void incrementInventory(InventoryUpdateRequest request) {
//        for (InventoryUpdateRequest.InventoryUpdateItem item : request.getItems()) {
//            InventoryId inventoryId = new InventoryId();
//            inventoryId.setSiteID(request.getSiteID());
//            inventoryId.setItemID(item.getItemID());
//            inventoryId.setItemLocation(""); // Default location
//
//            Inventory inventory = inventoryRepository.findById(inventoryId).orElse(null);

        for(InventoryUpdateRequest.InventoryUpdateItem item : request.getItems()) {
            Inventory inventory = inventoryRepository.findBySiteIdAndItemId(request.getSiteID(), item.getItemID());

            if (inventory != null) {
                // ✅ Existing inventory, just increase quantity
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            } else {
                // ✅ Inventory doesn't exist—create a new entry
                inventory = new Inventory();
                InventoryId inventoryId = new InventoryId();
                inventoryId.setSiteID(request.getSiteID());
                inventoryId.setItemID(item.getItemID());
                inventoryId.setItemLocation(""); // Default location
                inventory.setId(inventoryId); // ✅ Set the composite key
                inventory.setQuantity(item.getQuantity());
                inventory.setOptimumThreshold(0);
                inventory.setReorderThreshold(0);
                inventory.setNotes(""); // Optional: Set default notes if needed
            }

            // ✅ Save the updated or new inventory entry
            inventoryRepository.save(inventory);
        }
    }

    public List<Inventory> getSupplierItemsBelowThreshold() {
        List<Inventory> inventoryItems = inventoryRepository.findLowStockItems(warehouseID);
        List<Inventory> finalList = new ArrayList<>();

        for (Inventory inv : inventoryItems) {
            Item item = inv.getItem(); // Get the item details
            Supplier supplier = item.getSupplier();
            if (supplier.getActive() == (byte) 0) {
                System.out.printf("[DEBUG] Skipping active item because supplier inactive: %s\n", item.getName());
                continue;
            }
            if (item.getActive() == (byte) 0) {
                continue;
            }
            finalList.add(inv);
        }

        return finalList;
    }
}