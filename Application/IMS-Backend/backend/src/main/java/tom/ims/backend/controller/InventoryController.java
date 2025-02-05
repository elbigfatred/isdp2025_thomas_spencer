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

        InventoryId id = new InventoryId();
        id.setItemID(itemID);
        id.setSiteID(siteID);
        id.setItemLocation(itemLocation);

        try {
            Inventory inventory = inventoryService.updateInventory(id, updatedInventory);
            return inventory != null ? ResponseEntity.ok(inventory) : ResponseEntity.notFound().build();
        } catch (Exception e) {
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