package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.TxnRepository;
import tom.ims.backend.repository.TxnItemsRepository;
import tom.ims.backend.repository.TxnTypeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private TxnRepository txnRepository;

    @Autowired
    private TxnItemsRepository txnItemsRepository;

    // ✅ Check if an active order exists
    public boolean hasActiveStoreOrder(Integer siteId) {
        List<Txn> activeOrders = txnRepository.findActiveOrdersBySite(siteId);
        return !activeOrders.isEmpty();
    }

    public Txn createOrderTransaction(OrderRequest orderRequest) {
        if (hasActiveStoreOrder(orderRequest.getSiteIDTo())) {
            throw new RuntimeException("An active order already exists for this site.");
        }

        EmployeeService employeeService = new EmployeeService();
        SiteService siteService = new SiteService();
        TxnStatusService txnStatusService = new TxnStatusService();
        TxnTypeService txnTypeService = new TxnTypeService();
        InventoryService inventoryService = new InventoryService();

        Employee employee = employeeService.getEmployeeById(orderRequest.getEmployeeID());
        Site siteTo = siteService.getSiteById(orderRequest.getSiteIDTo());
        Site siteFrom = siteService.getSiteById(orderRequest.getSiteIDFrom()); // Default: Warehouse
        Txnstatus status = txnStatusService.findByName("NEW");
        Txntype type = txnTypeService.getbyTxnType("Store Order");

        Txn newOrder = new Txn();
        newOrder.setEmployeeID(employee);
        newOrder.setSiteIDTo(siteTo);
        newOrder.setSiteIDFrom(siteFrom);
        newOrder.setTxnStatus(status);
        newOrder.setTxnType(type);
        newOrder.setCreatedDate(LocalDateTime.now());
        newOrder.setShipDate(LocalDateTime.now().plusDays(7)); // Placeholder for delivery logic
        newOrder.setNotes(orderRequest.getNotes());

        Txn savedOrder = txnRepository.save(newOrder);
        ItemService itemService = new ItemService();

        // ✅ Auto-populate order with low stock items
        List<Inventory> lowStockItems = inventoryService.getItemsBelowThreshold(siteTo.getId());
        for (Inventory inv : lowStockItems) {
            Txnitem txnItem = new Txnitem();
            txnItem.settxnID(savedOrder);
            txnItem.setItemID(itemService.getItemById(inv.getId().getItemID()));
            txnItem.setQuantity(inv.getOptimumThreshold() - inv.getQuantity());
            txnItemsRepository.save(txnItem);
        }

        return savedOrder;
    }
}