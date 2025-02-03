package tom.ims.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private Integer employeeID; // Who is making the order
    private Integer siteIDTo; // Store receiving the order
    private Integer siteIDFrom; // Warehouse (Default)
    private String shipDate; // Delivery date
    private String notes; // Optional notes
    private List<OrderItem> items; // List of items in order

    public Integer getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
    }

    public Integer getSiteIDTo() {
        return siteIDTo;
    }

    public Integer getSiteIDFrom() {
        return siteIDFrom;
    }

    public String getShipDate() {
        return shipDate;
    }

    public String getNotes() {
        return notes;
    }

    public List<OrderItem> getItems() {
        return items;
    }


}