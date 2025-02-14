package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.*;
import tom.ims.backend.model.Txnitem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {



    @Autowired private TxnRepository txnRepository;
    @Autowired private TxnItemsRepository txnItemsRepository;
    @Autowired private TxnTypeRepository txnTypeRepository;
    @Autowired private EmployeeService employeeService;
    @Autowired private SiteService siteService;
    @Autowired private TxnStatusService txnStatusService;
    @Autowired private TxnTypeService txnTypeService;
    @Autowired private InventoryService inventoryService;
    @Autowired private ItemService itemService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SiteRepository siteRepository;


    // ✅ Check if an active order exists
    public boolean hasActiveStoreOrder(Integer siteId) {
        List<Txn> activeOrders = txnRepository.findActiveOrdersBySite(siteId);
        return !activeOrders.isEmpty();
    }

    public Txn createOrderTransaction(OrderRequest orderRequest) {
        if (hasActiveStoreOrder(orderRequest.getSiteIDTo())) {
            throw new RuntimeException("An active order already exists for this site.");
        }

        Employee employee = employeeService.getEmployeeById(orderRequest.getEmployeeID());
        Site siteTo = siteService.getSiteById(orderRequest.getSiteIDTo());
        Site siteFrom = siteService.getSiteById(orderRequest.getSiteIDFrom()); // Default: Warehouse
        Txnstatus status = txnStatusService.findByName("NEW");
        Txntype type = txnTypeService.getbyTxnType("Store Order");

        // ✅ Calculate the correct Ship Date based on the store's assigned dayOfWeek
        LocalDate shipDate = calculateNextDeliveryDate(siteTo.getDayOfWeek());

        Txn newOrder = new Txn();
        newOrder.setEmployeeID(employee);
        newOrder.setSiteIDTo(siteTo);
        newOrder.setSiteIDFrom(siteFrom);
        newOrder.setTxnStatus(status);
        newOrder.setTxnType(type);
        newOrder.setCreatedDate(LocalDateTime.now());
        newOrder.setShipDate(shipDate.atStartOfDay()); // Placeholder for delivery logic
        newOrder.setNotes(orderRequest.getNotes());
        newOrder.setEmergencyDelivery((byte) 0);

        // ✅ Generate and set barcode
        String generatedBarcode = "TXN-" + LocalDate.now().toString().replace("-", "") + "-" + (int) (Math.random() * 10000);
        newOrder.setBarCode(generatedBarcode);

        Txn savedOrder = txnRepository.save(newOrder);

        // ✅ Auto-populate order with low stock items
        List<Inventory> lowStockItems = inventoryService.getItemsBelowThreshold(siteTo.getId());
        System.out.println("[DEBUG] Found " + lowStockItems.size() + " items below reorder threshold for site " + siteTo.getId());

        for (Inventory inv : lowStockItems) {
            try {
                Txnitem txnItem = new Txnitem();
                Item item = itemService.getItemById(inv.getId().getItemID());

                if (item == null) {
                    System.out.println("[ERROR] Item not found for ID: " + inv.getId().getItemID());
                    continue; // Skip this item if it's missing
                }

                // ✅ Adjust quantity to match case size
                int caseSize = item.getCaseSize();
                int currentQty = inv.getQuantity();
                int neededQty = inv.getOptimumThreshold() - currentQty;

                System.out.println("[DEBUG] Processing item: " + item.getName() + " (Item ID: " + item.getId() + ")");
                System.out.println("  - Current Stock: " + currentQty);
                System.out.println("  - Case Size: " + caseSize);
                System.out.println("  - Needed Quantity (before case adjustment): " + neededQty);

                if (caseSize > 0) { // Avoid division by zero
                    int adjustedQty = (int) Math.ceil((double) neededQty / caseSize) * caseSize;
                    System.out.println("  - Adjusted Quantity (rounded to case size): " + adjustedQty);
                    txnItem.setQuantity(adjustedQty);
                } else {
                    System.out.println("[WARNING] Item " + item.getId() + " has an invalid case size (" + caseSize + ")");
                    txnItem.setQuantity(neededQty); // Fallback to raw quantity
                }

                // ✅ Set transaction and item details
                txnItem.setTxnAndItem(savedOrder, item);

                txnItemsRepository.save(txnItem);
                System.out.println("[INFO] Added txnItem: Item ID = " + inv.getId().getItemID() + ", Quantity = " + txnItem.getQuantity());

            } catch (Exception e) {
                System.out.println("[ERROR] Failed to save txnItem: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return savedOrder;
    }

    public List<Txn> getOrdersBySite(Integer siteId) {
        return txnRepository.findBySiteIDTo_Id(siteId);
    }

    public List<Txnitem> getTxnItemsByTxnId(Integer txnId) {
        return txnItemsRepository.findById_TxnID(txnId);
    }

    public void updateOrderItems(OrderUpdateRequest orderUpdateRequest) {
        Txn txn = txnRepository.findById(orderUpdateRequest.getTxnID())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ Remove all existing txn items for this order before adding new ones
        txnItemsRepository.deleteByTxnID(txn);

        for (OrderUpdateRequest.OrderItemUpdate itemUpdate : orderUpdateRequest.getItems()) {
            Item item = itemService.getItemById(itemUpdate.getItemID());

            if (item == null) {
                throw new RuntimeException("Item not found: " + itemUpdate.getItemID());
            }

            // ✅ Properly construct the composite key
            TxnitemId txnitemId = new TxnitemId();
            txnitemId.setTxnID(txn.getId());
            txnitemId.setItemID(item.getId());

            // ✅ Create the transaction item
            Txnitem txnItem = new Txnitem();
            txnItem.setId(txnitemId); // ✅ Set composite key properly
            txnItem.setTxnID(txn);
            txnItem.setItemID(item);
            txnItem.setQuantity(itemUpdate.getQuantity());

            // ✅ Save the updated txn item
            txnItemsRepository.save(txnItem);
        }
    }

    // ✅ Fetch an Order by ID
    public Txn getOrderById(Integer txnId) {
        System.out.println("[DEBUG] Fetching order ID: " + txnId);
        return txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + txnId));
    }

    public Txn submitOrder(Integer txnId) {
        // ✅ Retrieve the order
        Txn txn = txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ✅ Ensure order is in 'NEW' status
        if (!"NEW".equals(txn.getTxnStatus().getStatusName())) {
            throw new RuntimeException("Only NEW orders can be submitted");
        }

        // ✅ Fetch 'SUBMITTED' status from the database
        Txnstatus submittedStatus = txnStatusService.findByName("SUBMITTED");
        if (submittedStatus == null) {
            throw new RuntimeException("SUBMITTED status not found in database");
        }

        // ✅ Update order status
        txn.setTxnStatus(submittedStatus);
        Txn savedTxn = txnRepository.save(txn);

        System.out.println("[INFO] Order " + txnId + " submitted successfully.");

        return savedTxn;
    }

    public List<Txn> getOrdersByStatus(String status) {
        return txnRepository.findByTxnStatus_StatusName(status);
    }

    public LocalDate calculateNextDeliveryDate(String dayOfWeek) {
        DayOfWeek deliveryDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate today = LocalDate.now();

        // ✅ Step 1: Find the next Tuesday at 11:59 PM dynamically
        LocalDate nextTuesday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));

        // ✅ If today is already Tuesday, move to next week's Tuesday
        if (today.getDayOfWeek() == DayOfWeek.TUESDAY && today.isAfter(nextTuesday.minusDays(1))) {
            nextTuesday = nextTuesday.plusWeeks(1);
        }

        // ✅ Step 2: Find the Monday AFTER the next Tuesday
        LocalDate nextMonday = nextTuesday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        // ✅ Step 3: Assign the ship date based on nextMonday's week
        LocalDate nextWeekDeliveryDay = nextMonday.with(TemporalAdjusters.nextOrSame(deliveryDay));

        return nextWeekDeliveryDay;
    }

    public boolean hasActiveEmergencyOrder(Integer siteId) {
        List<Txn> activeOrders = txnRepository.findActiveEmergencyOrdersBySite(siteId);
        return !activeOrders.isEmpty();
    }

    public Txn createEmergencyOrderTransaction(OrderRequest orderRequest) {
        if (hasActiveEmergencyOrder(orderRequest.getSiteIDTo())) {
            throw new RuntimeException("An active emergency order already exists for this site.");
        }

        Employee employee = employeeService.getEmployeeById(orderRequest.getEmployeeID());
        Site siteTo = siteService.getSiteById(orderRequest.getSiteIDTo());
        Site siteFrom = siteService.getSiteById(orderRequest.getSiteIDFrom()); // Default: Warehouse
        Txnstatus status = txnStatusService.findByName("NEW");
        Txntype type = txnTypeService.getbyTxnType("Emergency Order");

        // ✅ Emergency orders should ship the next day in theory
        LocalDate shipDate = LocalDate.now().plusDays(1);

        Txn newOrder = new Txn();
        newOrder.setEmployeeID(employee);
        newOrder.setSiteIDTo(siteTo);
        newOrder.setSiteIDFrom(siteFrom);
        newOrder.setTxnStatus(status);
        newOrder.setTxnType(type);
        newOrder.setCreatedDate(LocalDateTime.now());
        newOrder.setShipDate(shipDate.atStartOfDay());
        newOrder.setNotes(orderRequest.getNotes());
        newOrder.setEmergencyDelivery((byte) 1);

        // ✅ Generate a distinct emergency barcode
        String generatedBarcode = "EM-TXN-" + LocalDate.now().toString().replace("-", "") + "-" + (int) (Math.random() * 10000);
        newOrder.setBarCode(generatedBarcode);

        return txnRepository.save(newOrder);
    }

    public Txn submitEmergencyOrder(Integer txnId) {
        Txn emergencyOrder = txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Emergency Order not found"));

        if (!"Emergency Order".equals(emergencyOrder.getTxnType().getTxnType())) {
            throw new RuntimeException("Txn ID " + txnId + " is not an emergency order.");
        }

        // ✅ Set ship date to the next day upon submission
        LocalDateTime shipDate = LocalDateTime.now().plusDays(1);
        emergencyOrder.setShipDate(shipDate);

        // ✅ Update status to SUBMITTED
        Txnstatus submittedStatus = txnStatusService.findByName("SUBMITTED");
        emergencyOrder.setTxnStatus(submittedStatus);

        return txnRepository.save(emergencyOrder);
    }

    public Txn updateOrderStatus(int txnId, String status) {
        Txn order = getOrderById(txnId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        Txnstatus newStatus = txnStatusService.findByName(status);
        order.setTxnStatus(newStatus);
        return txnRepository.save(order);
    }

    public Txn createOrUpdateBackorderTransaction(BackorderRequest backorderRequest) {
        Integer siteID = backorderRequest.getSiteID();
        List<Txnitem> itemsToAdd = backorderRequest.getItems().stream()
                .map(backorderItem -> {
                    Txnitem txnItem = new Txnitem();

                    // ✅ Set TxnitemId (composite key)
                    TxnitemId txnItemId = new TxnitemId();
                    txnItemId.setItemID(backorderItem.getItemID());

                    // ✅ Set fields
                    txnItem.setId(txnItemId);
                    txnItem.setItemID(itemService.getItemById(backorderItem.getItemID())); // Fetch Item entity
                    txnItem.setQuantity(backorderItem.getQuantity());

                    return txnItem;
                })
                .collect(Collectors.toList());

        // ✅ Step 1: Check for existing "NEW" backorder
        Txn existingBackorder = txnRepository.findNewBackorderBySite(siteID);

        if (existingBackorder != null) {
            System.out.println("[INFO] Existing backorder found for site " + siteID + ", adding items...");
            addItemsToExistingTxn(existingBackorder.getId(), itemsToAdd);
            return existingBackorder;
        }

        // ✅ Step 2: Create a new backorder transaction
        System.out.println("[INFO] No existing backorder found for site " + siteID + ", creating a new one...");

        Txn backorder = new Txn();
        backorder.setSiteIDTo(siteService.getSiteById(siteID));
        backorder.setSiteIDFrom(siteService.getWarehouseSite()); // Warehouse as source
        backorder.setEmployeeID(employeeService.getSystemUser()); // System-generated backorder
        backorder.setTxnStatus(txnStatusService.findByName("NEW"));
        backorder.setTxnType(txnTypeRepository.findByTypeName("Back Order"));
        // ✅ Generate a distinct barcode for the backorder
        String generatedBarcode = "BO-TXN-" + LocalDate.now().toString().replace("-", "") + "-" + (int) (Math.random() * 10000);
        backorder.setBarCode(generatedBarcode);
        backorder.setNotes("System-generated backorder");
        backorder.setCreatedDate(LocalDateTime.now()); // ✅ Set creation date to now

        // ✅ Set ship date (default: next available warehouse processing day)
        backorder.setShipDate(LocalDateTime.now().plusDays(1)); // Adjust as needed

        Txn savedBackorder = txnRepository.save(backorder);

        // ✅ Step 3: Add items to the new backorder
        addItemsToExistingTxn(savedBackorder.getId(), itemsToAdd);

        return savedBackorder;
    }

    public void addItemsToExistingTxn(Integer txnId, List<Txnitem> itemsToAdd) {
        Txn txn = txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Backorder not found with ID: " + txnId));

        for (Txnitem newItem : itemsToAdd) {
            Item item = itemService.getItemById(newItem.getItemID().getId());

            if (item == null) {
                throw new RuntimeException("Item not found: " + newItem.getItemID().getId());
            }

            // Check if item already exists in the backorder
            Txnitem existingTxnItem = txnItemsRepository.findByTxnIDAndItemID(txn, item);

            if (existingTxnItem != null) {
                // ✅ If it exists, just increase the quantity
                existingTxnItem.setQuantity(existingTxnItem.getQuantity() + newItem.getQuantity());
                txnItemsRepository.save(existingTxnItem);
                System.out.println("[INFO] Updated quantity for existing item: " + item.getId());
            } else {
                // ✅ Otherwise, create a new entry
                TxnitemId txnitemId = new TxnitemId();
                txnitemId.setTxnID(txnId);
                txnitemId.setItemID(item.getId());

                newItem.setId(txnitemId);
                newItem.setTxnID(txn);
                newItem.setItemID(item);

                txnItemsRepository.save(newItem);
                System.out.println("[INFO] Added new item to backorder: " + item.getId());
            }
        }
    }
}