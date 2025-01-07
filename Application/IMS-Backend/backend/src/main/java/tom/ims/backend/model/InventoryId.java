package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class InventoryId implements Serializable {
    private static final long serialVersionUID = -1249911908718529760L;
    @Column(name = "itemID", nullable = false)
    private Integer itemID;

    @Column(name = "siteID", nullable = false)
    private Integer siteID;

    @ColumnDefault("'Stock'")
    @Column(name = "itemLocation", nullable = false, length = 9)
    private String itemLocation;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InventoryId entity = (InventoryId) o;
        return Objects.equals(this.itemID, entity.itemID) &&
                Objects.equals(this.siteID, entity.siteID) &&
                Objects.equals(this.itemLocation, entity.itemLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID, siteID, itemLocation);
    }

}