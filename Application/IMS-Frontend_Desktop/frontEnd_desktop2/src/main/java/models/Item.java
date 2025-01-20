package models;

import java.math.BigDecimal;

public class Item {
    private Integer id;
    private String name;
    private String sku;
    private String description;
    private Category category;
    private BigDecimal weight;
    private Integer caseSize;
    private BigDecimal costPrice;
    private BigDecimal retailPrice;
    private String notes;
    private Boolean active;
    private String imageLocation;
    private Supplier supplier;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getCaseSize() {
        return caseSize;
    }

    public void setCaseSize(Integer caseSize) {
        this.caseSize = caseSize;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public String toString() {
        return "Item {" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", description='" + description + '\'' +
                ", category=" + (category != null ? category.toString() : "null") +
                ", weight=" + weight +
                ", caseSize=" + caseSize +
                ", costPrice=" + costPrice +
                ", retailPrice=" + retailPrice +
                ", notes='" + notes + '\'' +
                ", active=" + active +
                ", imageLocation='" + imageLocation + '\'' +
                ", supplier=" + (supplier != null ? supplier.toString() : "null") +
                '}';
    }
}
