package tom.ims.backend.controller;

import org.springframework.core.io.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tom.ims.backend.model.*;
import tom.ims.backend.repository.InventoryRepository;
import tom.ims.backend.service.CategoryService;
import tom.ims.backend.service.ItemService;
import tom.ims.backend.service.SiteService;
import tom.ims.backend.service.SupplierService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ItemService itemService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    SiteService siteService;
    @Autowired
    InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        try {
            List<Item> items = itemService.getAllItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Integer id) {
        try {
            Item item = itemService.getItemById(id);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateItem(@PathVariable int id) {
        try {
            // Fetch the item by ID
            Item item = itemService.getItemById(id);

            // Deactivate the item
            item.setActive((byte) 0);

            // Save the updated item
            itemService.saveItem(item);

            return ResponseEntity.ok("Item deactivated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deactivating the item.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateItem(
            @RequestParam("id") int id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "desc", required = false) String desc,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam(value = "supplier", required = false) int supplierId,
            @RequestParam(value = "caseSize", required = false) int caseSize,
            @RequestParam(value = "weight", required = false) BigDecimal weight,
            @RequestParam(value = "active", required = false) Byte active,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // Retrieve the item from the database
            Item item = itemService.getItemById(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }
            if (name != null) {
                item.setName(name);
            }

            // Update notes if provided
            if (notes != null) {
                item.setNotes(notes);
            }
            // Update description if provided
            if (desc != null) {
                item.setDescription(desc);
            }

            if (categoryName != null) {
                Category category = categoryService.getCategoryByName(categoryName);
                if (category != null) {
                    item.setCategory(category);
                }
            }

            if(supplierId > 0){
                Optional<Supplier> supplier = supplierService.getSupplierById(supplierId);
                if (supplier.isPresent()) {
                    item.setSupplier(supplier.get());
                }
            }

            // Update case size if provided
            if (caseSize > 0) item.setCaseSize(caseSize);

            // Update weight if provided
            if (weight != null) item.setWeight(weight);

            // Update active status if provided
            if (active != null) item.setActive(active);

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String filePath = saveFile(file, id); // Save the file and get the path
                item.setImageLocation(filePath);      // Update item's image location
            }

            // Save updated item
            itemService.saveItem(item);
            return ResponseEntity.ok("Item updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating item");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createItem(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "desc", required = false) String desc,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam(value = "supplier", required = false) int supplierId,
            @RequestParam(value = "caseSize", required = false) int caseSize,
            @RequestParam(value = "weight", required = false) BigDecimal weight,
            @RequestParam(value = "active", required = false) Byte active,
            @RequestParam(value = "costPrice") BigDecimal costPrice,
            @RequestParam(value = "retailPrice") BigDecimal retailPrice,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // Create a new item
            Item newItem = new Item();

            // Set name
            newItem.setName(name);

            // Set notes if provided
            if (notes != null) {
                newItem.setNotes(notes);
            }

            // Set description if provided
            if (desc != null) {
                newItem.setDescription(desc);
            }

            // Set category if provided
            if (categoryName != null) {
                Category category = categoryService.getCategoryByName(categoryName);
                if (category != null) {
                    newItem.setCategory(category);
                }
            }

            // Set supplier if provided
            if (supplierId > 0) {
                Optional<Supplier> supplier = supplierService.getSupplierById(supplierId);
                if (supplier.isPresent()) {
                    newItem.setSupplier(supplier.get());
                }
            }

            // Set case size if provided
            if (caseSize > 0) newItem.setCaseSize(caseSize);

            // Set weight if provided
            if (weight != null) newItem.setWeight(weight);

            // Set active status if provided
            if (active != null) newItem.setActive(active);

            // Set cost and retail prices
            newItem.setCostPrice(costPrice);
            newItem.setRetailPrice(retailPrice);

            // Generate SKU (example: prefix + timestamp)
            String generatedSKU = "SKU-" + System.currentTimeMillis();
            newItem.setSku(generatedSKU);  // Set the generated SKU

            // Save the new item
            itemService.saveItem(newItem);

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                String filePath = saveFile(file, newItem.getId()); // Save the file and get the path
                newItem.setImageLocation(filePath); // Update item's image location
            }

            itemService.saveItem(newItem);

            List<Site> allSites = siteService.getAllSites();
            for (Site site : allSites) {
                Inventory inventory = new Inventory();
                InventoryId inventoryId = new InventoryId();
                inventoryId.setItemID(newItem.getId());
                inventoryId.setSiteID(site.getId());
                inventoryId.setItemLocation("");

                inventory.setId(inventoryId);
                inventory.setQuantity(0);  // Set quantity to 0
                inventory.setReorderThreshold(0);  // Set reorderThreshold to 0
                inventory.setOptimumThreshold(0);  // Set optimumReorderThreshold to 0

                // Save inventory for the site
                inventoryRepository.save(inventory);
            }


            return ResponseEntity.status(HttpStatus.CREATED).body("Item created successfully with SKU: " + generatedSKU);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating item");
        }
    }

    private String saveFile(MultipartFile file, int itemId) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Create a unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = "item_" + itemId + fileExtension;

        // Save the file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString(); // Return the file path for storage in the DB
    }


    @GetMapping("/image/{itemId}")
    public ResponseEntity<Resource> getItemImage(@PathVariable int itemId) {
        try {

            System.out.println("Getting image");
            Item item = itemService.getItemById(itemId);
            Path imagePath = Paths.get(item.getImageLocation()).toAbsolutePath();

            System.out.println(imagePath + " is the path");

            // Load the file as a resource\
            Resource resource = new UrlResource(imagePath.toUri());

            // Check if the file exists
            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("Could not read the image");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Return the image as a response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body((Resource) resource);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}

