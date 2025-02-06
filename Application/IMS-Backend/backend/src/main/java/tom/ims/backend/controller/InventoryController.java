package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Inventory;
import tom.ims.backend.model.InventoryId;
import tom.ims.backend.service.InventoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // ✅ Get all inventory
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // ✅ Get inventory by site ID
    @GetMapping("/site/{siteID}")
    public ResponseEntity<List<Inventory>> getInventoryBySite(@PathVariable Integer siteID) {
        List<Inventory> inventory = inventoryService.getInventoryBySite(siteID);
        return inventory.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(inventory);
    }

    // ✅ Get a single inventory item by composite key
    @GetMapping("/{itemID}/{siteID}/{itemLocation}")
    public ResponseEntity<Inventory> getInventoryById(
            @PathVariable Integer itemID,
            @PathVariable Integer siteID,
            @PathVariable String itemLocation) {

        InventoryId id = new InventoryId();
        id.setItemID(itemID);
        id.setSiteID(siteID);
        id.setItemLocation(itemLocation);

        Optional<Inventory> inventory = inventoryService.getInventoryById(id);
        return inventory.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Add new inventory
    @PostMapping("/add")
    public ResponseEntity<Inventory> addInventory(@RequestBody Inventory inventory) {
        try {
            Inventory newInventory = inventoryService.saveInventory(inventory);
            return ResponseEntity.ok(newInventory);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Update existing inventory
    @PutMapping("/edit/{itemID}/{siteID}/{itemLocation}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable Integer itemID,
            @PathVariable Integer siteID,
            @PathVariable String itemLocation,
            @RequestBody Inventory updatedInventory) {

        System.out.println("[DEBUG] Received PUT request to update inventory.");
        System.out.println("[DEBUG] Path Variables -> itemID: " + itemID + ", siteID: " + siteID + ", itemLocation: " + itemLocation);

        // Log incoming JSON payload details
        System.out.println("[DEBUG] Received Inventory Object:");
        System.out.println("  - Reorder Threshold: " + updatedInventory.getReorderThreshold());
        System.out.println("  - Optimum Threshold: " + updatedInventory.getOptimumThreshold());
        System.out.println("  - Notes: " + updatedInventory.getNotes());
        System.out.println("  - Inventory ID Object: " + updatedInventory.getId());

        InventoryId id = new InventoryId();
        id.setItemID(itemID);
        id.setSiteID(siteID);
        id.setItemLocation(itemLocation);

        System.out.println("[DEBUG] Constructed InventoryId Object -> " + id.toString());

        // ✅ Fetch inventory by composite key
        List<Inventory> inventoryList = inventoryService.getInventoryBySiteAndItem(siteID, itemID);

        Inventory inventoryToUpdate = null;

        // ✅ Find the correct inventory entry by matching itemLocation
        for (Inventory inventoryItem : inventoryList) {
            if (inventoryItem.getId().getItemLocation().equals(itemLocation)) {
                inventoryToUpdate = inventoryItem;
                break;
            }
        }

        // ❌ If no matching inventory item is found, return 404
        if (inventoryToUpdate == null) {
            System.out.println("[ERROR] No inventory record found for itemID: " + itemID + ", siteID: " + siteID + ", itemLocation: " + itemLocation);
            return ResponseEntity.notFound().build();
        }

        System.out.println("[INFO] Found Inventory Entry -> ID: " + inventoryToUpdate.getId());

        // ✅ Update fields with new values
        inventoryToUpdate.setReorderThreshold(updatedInventory.getReorderThreshold());
        inventoryToUpdate.setOptimumThreshold(updatedInventory.getOptimumThreshold());
        inventoryToUpdate.setNotes(updatedInventory.getNotes());

        // ✅ Save the updated inventory
        try {
            inventoryService.saveInventory(inventoryToUpdate);
            System.out.println("[SUCCESS] Inventory updated.");
            return ResponseEntity.ok(inventoryToUpdate);
        } catch (Exception e) {
            System.out.println("[ERROR] Exception occurred while updating inventory:");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Delete inventory entry
    @DeleteMapping("/delete/{itemID}/{siteID}/{itemLocation}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable Integer itemID,
            @PathVariable Integer siteID,
            @PathVariable String itemLocation) {

        InventoryId id = new InventoryId();
        id.setItemID(itemID);
        id.setSiteID(siteID);
        id.setItemLocation(itemLocation);

        return inventoryService.deleteInventory(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}