package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @Column(name = "employeeID", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "Password", nullable = false, length = 255)
    @JsonProperty("password")
    private String password;

    @Column(name = "FirstName", nullable = false, length = 20)
    @JsonProperty("firstname")
    private String firstName;

    @Column(name = "LastName", nullable = false, length = 20)
    @JsonProperty("lastname")
    private String lastName;

    @Column(name = "Email", length = 100)
    @JsonProperty("email")
    private String email;

    @Column(name = "username", nullable = false)
    @JsonProperty("username")
    private String username;

    @Column(name = "notes")
    @JsonProperty("notes")
    private String notes;

    @ColumnDefault("0")
    @Column(name = "locked")
    @JsonProperty("locked")
    private Byte locked;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    @JsonProperty("active")
    private Byte active;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "siteID", nullable = false, referencedColumnName = "siteID")
    @JsonProperty("site")
    private Site site;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("roles")
    @JsonManagedReference
    private List<UserPosn> roles = new ArrayList<>();

    // Explicit Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Byte getLocked() {
        return locked;
    }

    public void setLocked(Byte locked) {
        this.locked = locked;
    }

    public Byte getActive() {
        return active;
    }

    public void setActive(Byte active) {
        this.active = active;
    }

    public Site getSite() {
        return site;
    }
    public void setSite(Site site) {
        this.site = site;
    }


    @Override
    public String toString() {
        StringBuilder posnBuilder = new StringBuilder();
        if (roles != null && !roles.isEmpty()) {
            for (UserPosn userPosn : roles) {
                Posn posn = userPosn.getPosn();
                if (posn != null) {
                    posnBuilder.append(posn.getPermissionLevel()).append(", ");
                }
            }
            // Remove trailing comma and space, if present
            if (posnBuilder.length() > 0) {
                posnBuilder.setLength(posnBuilder.length() - 2);
            }
        } else {
            posnBuilder.append("No Roles");
        }

        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", notes='" + notes + '\'' +
                ", locked=" + locked +
                ", active=" + active +
                ", site=" + site +
                ", roles=[" + posnBuilder.toString() + "]" +
                '}';
    }
}