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

    // ✅ Create a new transaction record in `txn`
    public Txn createOrderTransaction(OrderRequest orderRequest) {
        Txn newOrder = new Txn();
        EmployeeService employeeService = new EmployeeService();
        SiteService siteService = new SiteService();
        TxnStatusService txnStatusService = new TxnStatusService();
        TxnTypeService txnTypeService = new TxnTypeService();
        ItemService itemService = new ItemService();
        // ✅ Ensure LocalDateTime is used for DATETIME fields
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        newOrder.setEmployeeID(employeeService.getEmployeeById(orderRequest.getEmployeeID())); // Who placed the order
        newOrder.setSiteIDTo(siteService.getSiteById(orderRequest.getSiteIDTo())); // Store receiving the order
        newOrder.setSiteIDFrom(siteService.getSiteById(orderRequest.getSiteIDFrom())); // Default: Warehouse
        newOrder.setTxnStatus(txnStatusService.findByName("NEW")); // Default status
        newOrder.setTxnType(txnTypeService.getbyTxnType("Store Order")); // Order type
        newOrder.setShipDate(LocalDateTime.parse(orderRequest.getShipDate(), formatter)); // Delivery date
        newOrder.setCreatedDate(LocalDateTime.now()); // Today’s date
        newOrder.setNotes(orderRequest.getNotes());

        // Save order in txn table
        Txn savedOrder = txnRepository.save(newOrder);

        // Add items to txnitems table
        orderRequest.getItems().forEach(item -> {
            Txnitem txnItem = new Txnitem();
            txnItem.settxnID(savedOrder);
            txnItem.setItemID(itemService.getItemById(item.getItemID()));
            txnItem.setQuantity(item.getQuantity());
            txnItemsRepository.save(txnItem);
        });

        return savedOrder;
    }
}