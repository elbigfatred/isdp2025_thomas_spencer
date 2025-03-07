package tom.ims.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tom.ims.backend.model.Supplier;
import tom.ims.backend.repository.SupplierRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // Get all active suppliers
    public List<Supplier> getAllSuppliers(boolean includeInactive) {
        return includeInactive ? supplierRepository.findAll() : supplierRepository.findByActive((byte) 1);
    }

    // Get supplier by ID
    public Optional<Supplier> getSupplierById(Integer id) {
        return supplierRepository.findById(id);
    }

    // Add new supplier
    @Transactional
    public Supplier addSupplier(Supplier supplier) {
        supplier.setActive((byte) 1); // Set active by default
        return supplierRepository.save(supplier);
    }

    // Update existing supplier
    @Transactional
    public Supplier updateSupplier(Integer id, Supplier updatedSupplier) {
        return supplierRepository.findById(id).map(existingSupplier -> {
            existingSupplier.setName(updatedSupplier.getName());
            existingSupplier.setAddress1(updatedSupplier.getAddress1());
            existingSupplier.setAddress2(updatedSupplier.getAddress2());
            existingSupplier.setCity(updatedSupplier.getCity());
            existingSupplier.setCountry(updatedSupplier.getCountry());
            existingSupplier.setProvince(updatedSupplier.getProvince());
            existingSupplier.setPostalcode(updatedSupplier.getPostalcode());
            existingSupplier.setPhone(updatedSupplier.getPhone());
            existingSupplier.setContact(updatedSupplier.getContact());
            existingSupplier.setNotes(updatedSupplier.getNotes());
            existingSupplier.setActive(updatedSupplier.getActive());
            return supplierRepository.save(existingSupplier);
        }).orElseThrow(() -> new RuntimeException("Supplier not found"));
    }
}