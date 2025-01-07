package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory {
    @EmbeddedId
    private InventoryId id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reorderThreshold")
    private Integer reorderThreshold;

    @Column(name = "optimumThreshold", nullable = false)
    private Integer optimumThreshold;

    @Column(name = "notes")
    private String notes;

}