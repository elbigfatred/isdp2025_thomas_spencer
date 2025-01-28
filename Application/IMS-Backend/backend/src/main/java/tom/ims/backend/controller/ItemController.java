package tom.ims.backend.controller;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tom.ims.backend.model.Item;
import tom.ims.backend.service.ItemService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ItemService itemService;

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
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Retrieve the item from the database
            Item item = itemService.getItemById(id);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
            }

            // Update notes if provided
            if (notes != null) {
                item.setNotes(notes);
            }

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



}

