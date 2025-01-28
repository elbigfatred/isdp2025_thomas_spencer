package models;

import java.util.List;

public class Employee {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private List<Posn> roles; // Updated to store a list of roles
    private int permissionID;
    private boolean active;         // New field for active status
    private Site site;
    private String password;
    private boolean locked;
    private String mainRole;   // New main role field


    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Posn> getRoles() {
        return roles;
    }

    public void setRoles(List<Posn> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                ", site=" + site +
                ", roles=" + roles +
                '}';
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLocked(int i) {
        this.locked = i != 0;
    }

    public String getPassword() {
        return password;
    }

    public boolean getLocked() {
        return locked;
    }

    public Site getSite() {
        return site;
    }

    public int getPermissionID() {
        return permissionID;
    }

    public void setPermissionID(int permissionID) {
        this.permissionID = permissionID;
    }

    // Getter & Setter for mainRole
    public String getMainRole() {
        return mainRole;
    }

    public void setMainRole(String mainRole) {
        this.mainRole = mainRole;
    }

}