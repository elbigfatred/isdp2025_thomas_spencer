package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "txn")
public class Txn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txnID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employeeID", nullable = false)
    private Employee employeeID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "siteIDTo", nullable = false)
    private Site siteIDTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "siteIDFrom", nullable = false)
    private Site siteIDFrom;

    @Column(name = "shipDate", nullable = false)
    private Instant shipDate;

    @Column(name = "barCode", nullable = false, length = 50)
    private String barCode;

    @ColumnDefault("current_timestamp()")
    @Column(name = "createdDate", nullable = false)
    private Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryID")
    private Delivery deliveryID;

    @Column(name = "emergencyDelivery")
    private Byte emergencyDelivery;

    @Column(name = "notes")
    private String notes;

}