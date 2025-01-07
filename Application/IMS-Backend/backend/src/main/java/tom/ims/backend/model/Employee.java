package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeID", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "Password", nullable = false, length = 32)
    @JsonProperty("password")
    private String password;

    @Getter
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
    private String notes;

    @ColumnDefault("0")
    @JsonProperty("locked")
    @Column(name = "locked")
    private Byte locked;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

//    Override tostring
    @Override
    public String toString() {
        return "Employee{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", username=" + username + '}';
    }

}