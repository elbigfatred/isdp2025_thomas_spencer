package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.Inventory;
import tom.ims.backend.model.OrderItem;
import tom.ims.backend.repository.InventoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // ✅ Fetch inventory items that need reordering for a site
    public List<Inventory> getItemsBelowThreshold(Integer siteId) {
        List<Inventory> inventoryItems = inventoryRepository.findLowStockItems(siteId);

        return inventoryItems;

//        return inventoryItems.stream().map(item -> {
//            int quantityNeeded = item.getOptimumThreshold() - item.getQuantity();
//            int orderQuantity = Math.max(0, (quantityNeeded));
//
//            return new OrderItem(item.getId().getItemID(), item.getId().getSiteID(), item.getQuantity(), item.getReorderThreshold(), item.getOptimumThreshold(), orderQuantity);
//        }).collect(Collectors.toList());
    }
}