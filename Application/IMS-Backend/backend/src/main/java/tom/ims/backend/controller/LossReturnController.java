package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.*;
import tom.ims.backend.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/loss-return")
public class LossReturnController {

    @Autowired TxnService txnService;
    @Autowired TxnItemsRepository txnItemsRepository;
    @Autowired private OrderService orderService;
    @Autowired private InventoryService inventoryService;
    @Autowired private TxnRepository txnRepository;
    @Autowired private TxnTypeService txnTypeService;
    @Autowired private TxnStatusService statusService;
    @Autowired private TxnStatusService txnStatusService;
    @Autowired private TxnTypeRepository txnTypeRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private TxnStatusRepository txnStatusRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TxnauditService txnauditService;
    @Autowired private ItemService itemService;
    @Autowired private SiteService siteService;

    @PostMapping("/create")
    public ResponseEntity<?> createLossReturnTxn(@RequestBody LossReturnDTO dto) {
        try {
            // 1. Build and save Txn
            Txn txn = new Txn();
            txn.setSiteIDFrom(siteRepository.findById(dto.getSiteId()).orElseThrow());
            txn.setSiteIDTo(txn.getSiteIDFrom());
            Employee employee = employeeRepository.findById(dto.getEmployeeId()).orElseThrow();
            txn.setEmployeeID(employee);
            txn.setTxnType(txnTypeRepository.findByTypeName(dto.getTxnType()));
            txn.setTxnStatus(txnStatusService.findByName("SUBMITTED"));
            txn.setCreatedDate(LocalDateTime.now());
            txn.setShipDate(LocalDateTime.now());
            txn.setBarCode(generateLossReturnBarcode());
            txn.setNotes(dto.getNotes());

            Txn savedTxn = txnRepository.save(txn);

            // 2. Build and save TxnItem
            Txnitem item = new Txnitem();
            item.setTxnAndItem(savedTxn, itemRepository.findById(dto.getItemId()).orElseThrow());
            item.setQuantity(dto.getQuantity());
            item.setNotes(dto.getItemNotes());

            txnItemsRepository.save(item);

            txnauditService.createAuditEntry(txn, employee,"Created " + dto.getTxnType() + " transaction manually."
            );

            // 3. Handle resellable logic if RETURN
            if ("RETURN".equalsIgnoreCase(dto.getTxnType()) && !dto.isResellable()) {
                Txn lossTxn = new Txn();
                lossTxn.setSiteIDFrom(txn.getSiteIDFrom());
                lossTxn.setSiteIDTo(lossTxn.getSiteIDFrom());
                lossTxn.setEmployeeID(txn.getEmployeeID());
                lossTxn.setTxnType(txnTypeRepository.findByTypeName("LOSS"));
                lossTxn.setTxnStatus(txnStatusService.findByName("SUBMITTED"));
                lossTxn.setCreatedDate(LocalDateTime.now());
                lossTxn.setShipDate(LocalDateTime.now());
                lossTxn.setBarCode(generateLossReturnBarcode());
                lossTxn.setNotes("Auto LOSS for non-resellable RETURN");

                Txn savedLossTxn = txnRepository.save(lossTxn);

                Txnitem lossItem = new Txnitem();
                lossItem.setTxnAndItem(savedLossTxn, item.getItemID());
                lossItem.setQuantity(dto.getQuantity());
                lossItem.setNotes("Auto LOSS");

                txnItemsRepository.save(lossItem);
                txnauditService.createAuditEntry(lossTxn, txn.getEmployeeID(),"Auto-generated LOSS transaction for non-resellable RETURN."
                );
            }

            return ResponseEntity.ok(savedTxn.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating transaction");
        }
    }


    private String generateLossReturnBarcode() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dateFormatter);
        int randomSuffix = new Random().nextInt(9000) + 1000; // 4-digit suffix
        return "LR-" + datePart + "-" + randomSuffix;
    }

}
