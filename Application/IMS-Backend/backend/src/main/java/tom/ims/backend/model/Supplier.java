package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplierID", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "address1", nullable = false, length = 50)
    private String address1;

    @Column(name = "address2", length = 50)
    private String address2;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province", nullable = false)
    private Province province;

    @Column(name = "postalcode", nullable = false, length = 11)
    private String postalcode;

    @Column(name = "phone", nullable = false, length = 14)
    private String phone;

    @Column(name = "contact", length = 100)
    private String contact;

    @Column(name = "notes")
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}