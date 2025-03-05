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

    @ManyToOne
    @JoinColumn(name = "vehicleType", referencedColumnName = "vehicleType", nullable = false)
    private Vehicle vehicle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Instant deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public BigDecimal getDistanceCost() {
        return distanceCost;
    }

    public void setDistanceCost(BigDecimal distanceCost) {
        this.distanceCost = distanceCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

}