package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "siteID", nullable = false)
    private Integer id;

    @Column(name = "siteName", nullable = false, length = 50)
    private String siteName;

    @Column(name = "address", nullable = false, length = 50)
    private String address;

    @Column(name = "address2", length = 50)
    private String address2;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provinceID", nullable = false, referencedColumnName = "provinceID")
    private Province provinceID;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "postalCode", nullable = false, length = 14)
    private String postalCode;

    @Column(name = "phone", nullable = false, length = 14)
    private String phone;

    @Column(name = "dayOfWeek", length = 50)
    private String dayOfWeek;

    @Column(name = "distanceFromWH", nullable = false)
    private Integer distanceFromWH;

    @Column(name = "notes")
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}