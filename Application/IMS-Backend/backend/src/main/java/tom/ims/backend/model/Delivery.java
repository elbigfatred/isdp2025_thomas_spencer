package tom.ims.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Data
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deliveryID", nullable = false)
    private Integer id;

    @Column(name = "deliveryDate", nullable = false)
    private Instant deliveryDate;

    @Column(name = "distanceCost", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceCost;

    @Column(name = "notes")
    private String notes;

}