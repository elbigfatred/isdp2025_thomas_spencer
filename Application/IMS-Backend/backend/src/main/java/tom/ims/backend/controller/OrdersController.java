package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.service.InventoryService;
import tom.ims.backend.service.OrderService;
import tom.ims.backend.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemService itemService;

    // ✅ Check if an active Store Order exists for this site
    @GetMapping("/check-active")
    public ResponseEntity<Boolean> checkActiveOrder(@RequestParam Integer siteId) {
        boolean hasActiveOrder = orderService.hasActiveStoreOrder(siteId);
        return ResponseEntity.ok(hasActiveOrder);
    }

    // ✅ Get items below reorder threshold (Prepopulate Order)
    @GetMapping("/prepopulate")
    public ResponseEntity<List<Inventory>> prepopulateOrder(@RequestParam Integer siteId) {
        List<Inventory> reorderItems = inventoryService.getItemsBelowThreshold(siteId);
        return ResponseEntity.ok(reorderItems);
    }

    // ✅ Create a new Store Order transaction
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            if (orderService.hasActiveStoreOrder(orderRequest.getSiteIDTo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("An active order already exists for this site.");
            }

            Txn newOrder = orderService.createOrderTransaction(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }
}