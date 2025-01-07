package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @Column(name = "vehicleType", nullable = false, length = 20)
    private String vehicleType;

    @Column(name = "maxWeight", nullable = false, precision = 10)
    private BigDecimal maxWeight;

    @Column(name = "HourlyTruckCost", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyTruckCost;

    @Column(name = "costPerKm", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerKm;

    @Column(name = "notes", nullable = false)
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}