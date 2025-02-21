package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;
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
    @Autowired private TxnauditService txnauditService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private SiteRepository siteRepository;

    // === ORDER CREATION ===

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
        int addedItemCount = 0;
        int totalAddedItems = 0;

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
                int adjustedQty = 0;

                System.out.println("[DEBUG] Processing item: " + item.getName() + " (Item ID: " + item.getId() + ")");
                System.out.println("  - Current Stock: " + currentQty);
                System.out.println("  - Case Size: " + caseSize);
                System.out.println("  - Needed Quantity (before case adjustment): " + neededQty);

                if (caseSize > 0) { // Avoid division by zero
                    adjustedQty = (int) Math.ceil((double) neededQty / caseSize) * caseSize;
                    System.out.println("  - Adjusted Quantity (rounded to case size): " + adjustedQty);
                    txnItem.setQuantity(adjustedQty);
                } else {
                    System.out.println("[WARNING] Item " + item.getId() + " has an invalid case size (" + caseSize + ")");
                    txnItem.setQuantity(neededQty); // Fallback to raw quantity
                }

                // ✅ Set transaction and item details
                txnItem.setTxnAndItem(savedOrder, item);

                txnItemsRepository.save(txnItem);
                addedItemCount++; // ✅ Track number of items added
                totalAddedItems += txnItem.getQuantity();
                System.out.println("[INFO] Added txnItem: Item ID = " + inv.getId().getItemID() + ", Quantity = " + txnItem.getQuantity());

            } catch (Exception e) {
                System.out.println("[ERROR] Failed to save txnItem: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ✅ Build audit log
        String auditMessage = "Store order created by " + employee.getFirstName() + " " + employee.getLastName() +
                " at " + siteTo.getSiteName() + " on " + LocalDateTime.now() +
                ". Scheduled for delivery on " + shipDate +
                ". " + addedItemCount + " line items were automatically added due to low stock."
                + ". Total Quantity: " + totalAddedItems;

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(savedOrder, employee, auditMessage);

        return savedOrder;
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
        Txn savedOrder = txnRepository.save(newOrder);

        // ✅ Build audit entry
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Emergency order created by ").append(employee.getFirstName()).append(" ").append(employee.getLastName())
                .append(" at ").append(siteTo.getSiteName())
                .append(" on ").append(LocalDateTime.now())
                .append(". Scheduled for delivery on ").append(shipDate);

        // ✅ Save audit log
        txnauditService.createAuditEntry(savedOrder, employee, auditLog.toString());

        return savedOrder;
    }

    // === ORDER/ORDERITEM RETRIEVAL ===
    public List<Txn> getOrdersByStatus(String status) {
        return txnRepository.findByTxnStatus_StatusName(status);
    }

    public List<Txn> getOrdersBySite(Integer siteId) {
        return txnRepository.findBySiteIDTo_Id(siteId);
    }

    public List<Txnitem> getTxnItemsByTxnId(Integer txnId) {
        return txnItemsRepository.findById_TxnID(txnId);
    }

    public Txn getOrderById(Integer txnId) {
        System.out.println("[DEBUG] Fetching order ID: " + txnId);
        return txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + txnId));
    }

    // === ORDER UPDATING ===
    @Transactional
    public void updateOrderItems(OrderUpdateRequest orderUpdateRequest, String empUsername) {
        Txn txn = txnRepository.findById(orderUpdateRequest.getTxnID())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        System.out.println("[DEBUG] Updating order items: " + orderUpdateRequest);

        Employee employee = employeeRepository.findByUsername(empUsername).get();

        // ✅ Get count of line items before update (SIMPLE COUNT)
        int previousItemCount = txnItemsRepository.countByTxnID(txn.getId());
        System.out.println("[DEBUG] Line items before update: " + previousItemCount);

        // ✅ Remove all existing txn items for this order before adding new ones
        txnItemsRepository.deleteByTxnID(txn);
        txnItemsRepository.flush(); // ✅ Force immediate execution

        // ✅ Add new items
        int newItemCount = 0;
        int newItemTotal = 0;
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

            newItemCount++;
            newItemTotal += txnItem.getQuantity();
        }

        // ✅ Build concise audit log
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Order items updated by ").append(employee.getFirstName()).append(" ").append(employee.getLastName())
                .append(" on ").append(LocalDateTime.now()).append(" for Order ID: ").append(txn.getId())
                .append(". Line Items before update: ").append(previousItemCount)
                .append(", Line Items after update: ").append(newItemCount).append(".")
                .append(". Total Items after update: ").append(newItemTotal);

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(txn, employee, auditLog.toString());
        System.out.println("[DEBUG] auditLog: " + auditLog.toString());
    }


    public Txn updateOrderStatus(int txnId, String status, String empUsername) {
        Txn order = getOrderById(txnId);
        System.out.println("[DEBUG] Updating order status from: " + order.getTxnStatus());
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        String oldStatus = order.getTxnStatus().getStatusName();
        System.out.println("[DEBUG] Updating order status from: " + oldStatus);
        System.out.println("[DEBUG] Looking for order status to: " + status);

        Txnstatus newStatus = txnStatusService.findByName(status);

        System.out.println("[DEBUG] Updating order status to: " + newStatus);
        order.setTxnStatus(newStatus);
        Txn savedOrder = txnRepository.save(order);

        Employee employee = employeeRepository.findByUsername(empUsername).get();

        // ✅ Build audit log
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Order ID ").append(txnId)
                .append(" status updated from '").append(oldStatus)
                .append("' to '").append(status)
                .append("' by ").append(employee.getFirstName()).append(" ").append(employee.getLastName())
                .append(" on ").append(LocalDateTime.now());

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(savedOrder, employee, auditLog.toString());
        return savedOrder;
    }

    public void addItemsToExistingBackorderTxn(Integer txnId, List<Txnitem> itemsToAdd) {

        System.out.println("[DEBUG] Adding backorder txn items to: " + txnId);
        Txn txn = txnRepository.findById(txnId)
                .orElseThrow(() -> new RuntimeException("Backorder not found with ID: " + txnId));


        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Backorder ID ").append(txnId).append(" updated by System ")
                .append(" on ").append(LocalDateTime.now()).append(". ");

        int totalNewItems = 0;
        int totalUpdatedItems = 0;

        for (Txnitem newItem : itemsToAdd) {
            Item item = itemService.getItemById(newItem.getItemID().getId());

            if (item == null) {
                throw new RuntimeException("Item not found: " + newItem.getItemID().getId());
            }

            // Check if item already exists in the backorder
            Txnitem existingTxnItem = txnItemsRepository.findByTxnIDAndItemID(txn, item);

            if (existingTxnItem != null) {
                // ✅ If it exists, just increase the quantity
                int oldQuantity = existingTxnItem.getQuantity();

                existingTxnItem.setQuantity(existingTxnItem.getQuantity() + newItem.getQuantity());
                txnItemsRepository.save(existingTxnItem);

                totalNewItems++;
                System.out.println("[INFO] Updated quantity for existing item: " + item.getId());
                auditLog.append("Updated Item ID ").append(item.getId())
                        .append(" from ").append(oldQuantity)
                        .append(" to ").append(existingTxnItem.getQuantity()).append(". ");
            } else {
                // ✅ Otherwise, create a new entry
                TxnitemId txnitemId = new TxnitemId();
                txnitemId.setTxnID(txnId);
                txnitemId.setItemID(item.getId());

                newItem.setId(txnitemId);
                newItem.setTxnID(txn);
                newItem.setItemID(item);

                txnItemsRepository.save(newItem);
                totalNewItems++;
                auditLog.append("Added new Item ID ").append(item.getId())
                        .append(" with quantity ").append(newItem.getQuantity()).append(". ");

                System.out.println("[INFO] Added new item to backorder: " + item.getId());
            }
        }


        auditLog.append("Total Items Added: ").append(totalNewItems)
                .append(", Total Items Updated: ").append(totalUpdatedItems).append(".");

        // ✅ Save audit log
        Employee systemUser = employeeService.getEmployeeById(1000);
        txnauditService.createAuditEntry(txn, systemUser, auditLog.toString());
    }

    // === ORDER SUBMISSION & AUTO-PROCESSING ===
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

        Employee employee = txn.getEmployeeID();
        // ✅ Build audit log
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Order ID ").append(txnId).append(" submitted by ")
                .append(employee.getFirstName()).append(" ").append(employee.getLastName())
                .append(" on ").append(LocalDateTime.now());

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(savedTxn, employee, auditLog.toString());


        System.out.println("[INFO] Order " + txnId + " submitted successfully.");

        return savedTxn;
    }

    public Txn autoSubmitOrder(Integer txnId) {
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

        Employee employee = txn.getEmployeeID();
        // ✅ Build audit log
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Order ID ").append(txnId).append(" automatically submitted by System (regular order cutoff)")
                .append(" on ").append(LocalDateTime.now());

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(savedTxn, employee, auditLog.toString());


        System.out.println("[INFO] Order " + txnId + " submitted successfully.");

        return savedTxn;
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

        Txn savedTxn = txnRepository.save(emergencyOrder);

        // ✅ Get the employee who submitted the order
        Employee employee = emergencyOrder.getEmployeeID();
        // ✅ Build audit log
        StringBuilder auditLog = new StringBuilder();
        auditLog.append("Emergency Order ID ").append(txnId).append(" submitted by ")
                .append(employee.getFirstName()).append(" ").append(employee.getLastName())
                .append(" on ").append(LocalDateTime.now())
                .append(". Ship date set to ").append(shipDate.toLocalDate());

        // ✅ Save audit log **LAST**
        txnauditService.createAuditEntry(savedTxn, employee, auditLog.toString());

        return savedTxn;
    }

    // === ORDER VALIDATION HELPERS ===
    public boolean hasActiveStoreOrder(Integer siteId) {
        List<Txn> activeOrders = txnRepository.findActiveOrdersBySite(siteId);
        return !activeOrders.isEmpty();
    }

    public boolean hasActiveEmergencyOrder(Integer siteId) {
        List<Txn> activeOrders = txnRepository.findActiveEmergencyOrdersBySite(siteId);
        return !activeOrders.isEmpty();
    }

    // === BACKORDER PROCESSING ===
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
        Employee systemUser = employeeService.getEmployeeById(1000);
        StringBuilder auditLog = new StringBuilder();

        auditLog.append("Backorder updated by system user ")
                .append(" on ").append(LocalDateTime.now());

        if (existingBackorder != null) {
            System.out.println("[INFO] Existing backorder found for site " + siteID + ", adding items...");
            // ✅ Log old item count
            int oldItemCount = txnItemsRepository.countByTxnID(existingBackorder.getId());
            addItemsToExistingBackorderTxn(existingBackorder.getId(), itemsToAdd);
            int newItemCount = txnItemsRepository.countByTxnID(existingBackorder.getId());

            auditLog.append(". Previously contained ").append(oldItemCount).append(" items. Now contains ")
                    .append(newItemCount).append(" items.");

            // ✅ Save audit log **LAST**
            txnauditService.createAuditEntry(existingBackorder,systemUser, auditLog.toString());
            return existingBackorder;
        }

        // ✅ Step 2: Create a new backorder transaction
        System.out.println("[INFO] No existing backorder found for site " + siteID + ", creating a new one...");

        Txn backorder = new Txn();
        Site siteTo = siteService.getSiteById(siteID);
        backorder.setSiteIDTo(siteTo);
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
        LocalDate shipDate = calculateNextDeliveryDate(siteTo.getDayOfWeek());
        backorder.setShipDate(shipDate.atStartOfDay()); // Adjust as needed

        Txn savedBackorder = txnRepository.save(backorder);

        // ✅ Step 3: Add items to the new backorder
        addItemsToExistingBackorderTxn(savedBackorder.getId(), itemsToAdd);

        // ✅ Save audit log **LAST**
        auditLog.append(". Created a new backorder with ").append(itemsToAdd.size()).append(" items.");
        txnauditService.createAuditEntry(savedBackorder, systemUser, auditLog.toString());

        return savedBackorder;
    }

    // === DELIVERY SCHEDULING ===
    public LocalDate calculateNextDeliveryDate(String dayOfWeek) {
        DayOfWeek deliveryDay = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate today = LocalDate.now();

        // ✅ Step 1: Find the next Tuesday (cutoff deadline at 11:59 PM)
        LocalDate nextTuesday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));

        // ✅ Check if today is AFTER the current Tuesday cutoff
        LocalDate cutoffTime = nextTuesday.atTime(23, 59).toLocalDate();
        boolean isPastCutoff = today.isAfter(cutoffTime);

        // ✅ Step 2: Determine the correct cutoff Tuesday (either this or next week)
        LocalDate referenceTuesday = isPastCutoff ? nextTuesday.plusWeeks(1) : nextTuesday;

        // ✅ Step 3: Find the **next Sunday** after the cutoff Tuesday (start of delivery week)
        LocalDate nextSunday = referenceTuesday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // ✅ Step 4: Find the delivery day within that week
        LocalDate nextWeekDeliveryDay = nextSunday.with(TemporalAdjusters.nextOrSame(deliveryDay));

        return nextWeekDeliveryDay;
    }

    public void updateTxnShipDate(int txnId, String shipDate, String empUsername) {
        try {
            System.out.println("[DEBUG] Updating ship date for Order ID: " + txnId + " by " + empUsername);

            // ✅ Fetch transaction
            Txn txn = txnRepository.findById(txnId).orElseThrow(() ->
                    new IllegalArgumentException("Transaction not found: " + txnId));

            // ✅ Convert string to LocalDate
            LocalDateTime newShipDate = LocalDate.parse(shipDate).atStartOfDay();
            LocalDateTime oldShipDate = txn.getShipDate(); // Capture previous date

            // ✅ Fetch employee for logging
            Employee employee = employeeRepository.findByUsername(empUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + empUsername));

            // ✅ Convert string to LocalDate and update
            txn.setShipDate(LocalDate.parse(shipDate).atStartOfDay());

            // ✅ Save the transaction
            txnRepository.save(txn);

            // ✅ Build audit log message
            String auditMessage = "Ship date updated for Order ID " + txnId
                    + " by " + employee.getFirstName() + " " + employee.getLastName()
                    + " on " + LocalDateTime.now()
                    + ". Previous ship date: " + (oldShipDate != null ? oldShipDate.toLocalDate() : "None")
                    + ", New ship date: " + newShipDate.toLocalDate();

            // ✅ Save audit log
            txnauditService.createAuditEntry(txn, employee, auditMessage);

            System.out.println("[SUCCESS] Ship date updated successfully for Order ID: " + txnId);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to update ship date: " + e.getMessage());
            throw new RuntimeException("Failed to update ship date", e);
        }
    }

}