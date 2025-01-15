package models;

public class Posn {
    private int id; // Position ID
    private String permissionLevel; // Permission level (e.g., Administrator, Store Manager)
    private boolean active; // Indicates if the position is active

    // Constructor
    public Posn() {}

    public Posn(int id, String permissionLevel, boolean active) {
        this.id = id;
        this.permissionLevel = permissionLevel;
        this.active = active;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(String permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // ToString method for debugging
    @Override
    public String toString() {
        return this.permissionLevel; // This is what will display in the JComboBox
    }
}