package tom.ims.backend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserPosnKey implements Serializable {

    @Column(name = "userID")
    private Integer userID;

    @Column(name = "posnID")
    private Integer posnID;

    public UserPosnKey() {
    }

    public UserPosnKey(Integer userID, Integer posnID) {
        this.userID = userID;
        this.posnID = posnID;
    }

    // Getters and setters
    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getPosnID() {
        return posnID;
    }

    public void setPosnID(Integer posnID) {
        this.posnID = posnID;
    }

    // Override equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPosnKey that = (UserPosnKey) o;
        return Objects.equals(userID, that.userID) && Objects.equals(posnID, that.posnID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, posnID);
    }
}