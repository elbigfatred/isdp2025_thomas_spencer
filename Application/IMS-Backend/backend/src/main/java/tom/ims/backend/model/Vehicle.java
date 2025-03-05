package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("vehicleType")
    private String vehicleType;

    @Column(name = "maxWeight", nullable = false, precision = 10)
    @JsonProperty("maxWeight")
    private BigDecimal maxWeight;

    @Column(name = "HourlyTruckCost", nullable = false, precision = 10, scale = 2)
    @JsonProperty("HourlyTruckCost")
    private BigDecimal hourlyTruckCost;

    @Column(name = "costPerKm", nullable = false, precision = 10, scale = 2)
    @JsonProperty("costPerKm")
    private BigDecimal costPerKm;

    @Column(name = "notes", nullable = false)
    @JsonProperty("notes")
    private String notes;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    @JsonProperty("active")
    private Byte active;


    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(BigDecimal maxWeight) {
        this.maxWeight = maxWeight;
    }

    public BigDecimal getHourlyTruckCost() {
        return hourlyTruckCost;
    }

    public void setHourlyTruckCost(BigDecimal hourlyTruckCost) {
        this.hourlyTruckCost = hourlyTruckCost;
    }

    public BigDecimal getCostPerKm() {
        return costPerKm;
    }

    public void setCostPerKm(BigDecimal costPerKm) {
        this.costPerKm = costPerKm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Byte getActive() {
        return active;
    }

    public void setActive(Byte active) {
        this.active = active;
    }
}