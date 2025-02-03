package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {
    @EmbeddedId
    private InventoryId id;

//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "itemID", nullable = false, referencedColumnName = "itemID")
//    @JsonProperty("item")
//    private Item item;
//
//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "siteID", nullable = false, referencedColumnName = "siteID")
//    @JsonProperty("site")
//    private Site site;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "itemID", nullable = false, insertable = false, updatable = false)
    @JsonProperty("item") // ✅ Ensures the frontend gets the full item object
    private Item item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reorderThreshold")
    private Integer reorderThreshold;

    @Column(name = "optimumThreshold", nullable = false)
    private Integer optimumThreshold;

    @Column(name = "notes")
    private String notes;

    // ✅ GETTERS
    public InventoryId getId() {
        return id;
    }

//    public Item getItem() {
//        return item;
//    }
//
//    public Site getSite() {
//        return site;
//    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public Integer getOptimumThreshold() {
        return optimumThreshold;
    }

    public String getNotes() {
        return notes;
    }

    // ✅ SETTERS
    public void setId(InventoryId id) {
        this.id = id;
    }

//    public void setItem(Item item) {
//        this.item = item;
//    }
//
//    public void setSite(Site site) {
//        this.site = site;
//    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public void setOptimumThreshold(Integer optimumThreshold) {
        this.optimumThreshold = optimumThreshold;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}