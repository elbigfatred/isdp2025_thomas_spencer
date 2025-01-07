package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "txnaudit")
public class Txnaudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txnAuditID", nullable = false)
    private Integer id;

    @ColumnDefault("current_timestamp()")
    @Column(name = "createdDate", nullable = false)
    private Instant createdDate;

    @Column(name = "txnID", nullable = false)
    private Integer txnID;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employeeID", nullable = false)
    private Employee employeeID;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "txnDate", nullable = false)
    private Instant txnDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SiteID", nullable = false)
    private Site siteID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryID")
    private Delivery deliveryID;

    @Column(name = "notes")
    private String notes;

}