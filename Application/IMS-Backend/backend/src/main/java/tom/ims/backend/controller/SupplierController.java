package tom.ims.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.model.Province;
import tom.ims.backend.model.Supplier;
import tom.ims.backend.repository.ProvinceRepository;
import tom.ims.backend.service.ProvinceService;
import tom.ims.backend.service.SupplierService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService supplierService;
    private final ProvinceRepository provinceRepository;

    public SupplierController(SupplierService supplierService, ProvinceRepository provinceRepository, ProvinceService provinceService) {
        this.supplierService = supplierService;
        this.provinceRepository = provinceRepository;
    }

    //  Get all suppliers
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(includeInactive));
    }

    //  Get supplier by ID
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Integer id) {
        Optional<Supplier> supplier = supplierService.getSupplierById(id);
        return supplier.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //  Add supplier
    @PostMapping
    public ResponseEntity<?> addSupplier(@RequestBody Supplier supplier) {
        try {
            Province province = provinceRepository.findByProvinceID(supplier.getProvince().getProvinceID());
            supplier.setProvince(province);

            Supplier savedSupplier = supplierService.addSupplier(supplier);
            return ResponseEntity.ok(savedSupplier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating supplier: " + e.getMessage());
        }
    }

    //  Update supplier
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Integer id, @RequestBody Supplier supplier) {
        try {
            Province province = provinceRepository.findByProvinceID(supplier.getProvince().getProvinceID());
            supplier.setProvince(province);
            return ResponseEntity.ok(supplierService.updateSupplier(id, supplier));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}