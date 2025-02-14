package tom.ims.backend.model;

import java.util.List;

public class BackorderRequest {
    private Integer siteID;
    private Integer employeeID;  // 🔹 Employee processing the backorder
    private String notes;  // 🔹 Optional field for additional details
    private List<BackorderItem> items;  // 🔹 Use a DTO instead of `Txnitem`

    // ✅ Constructor
    public BackorderRequest(Integer siteID, Integer employeeID, String notes, List<BackorderItem> items) {
        this.siteID = siteID;
        this.employeeID = employeeID;
        this.notes = notes;
        this.items = items;
    }

    // ✅ GETTERS
    public Integer getSiteID() { return siteID; }
    public Integer getEmployeeID() { return employeeID; }
    public String getNotes() { return notes; }
    public List<BackorderItem> getItems() { return items; }

    // ✅ SETTERS
    public void setSiteID(Integer siteID) { this.siteID = siteID; }
    public void setEmployeeID(Integer employeeID) { this.employeeID = employeeID; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setItems(List<BackorderItem> items) { this.items = items; }
}