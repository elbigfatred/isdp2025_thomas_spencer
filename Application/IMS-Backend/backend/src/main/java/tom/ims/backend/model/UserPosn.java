package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "user_posn")
public class UserPosn {

    @EmbeddedId
    @JsonIgnore
    private UserPosnKey id; // Composite key

    @MapsId("userID") // Maps this part of the composite key
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty("employeeID")
    @JoinColumn(name = "userID", nullable = false)
    @JsonBackReference
    private Employee user;

    @MapsId("posnID") // Maps this part of the composite key
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty("posn")
    @JoinColumn(name = "posnID", nullable = false)
    private Posn posn;

    // Explicit Getters and Setters

    public UserPosnKey getId() {
        return id;
    }

    public void setId(UserPosnKey id) {
        this.id = id;
    }

    public Employee getUser() {
        return user;
    }

    public void setUser(Employee user) {
        this.user = user;
    }

    public Posn getPosn() {
        return posn;
    }

    public void setPosn(Posn posn) {
        this.posn = posn;
    }

    // Optional: toString Method
    @Override
    public String toString() {
        return "UserPosn{" +
                "id=" + id +
                ", user=" + user +
                ", posn=" + posn +
                '}';
    }
}