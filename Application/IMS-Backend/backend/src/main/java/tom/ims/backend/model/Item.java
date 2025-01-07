package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemID", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sku", nullable = false, length = 20)
    private String sku;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category", nullable = false)
    private Category category;

    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "caseSize", nullable = false)
    private Integer caseSize;

    @Column(name = "costPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "retailPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "notes")
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}