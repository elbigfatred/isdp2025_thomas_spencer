package tom.ims.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "posn")
public class Posn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "positionID", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "permissionLevel", nullable = false, length = 20)
    @JsonProperty("permissionLevel")
    private String permissionLevel;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    @JsonProperty("active")
    private Byte active;

    @Override
    public String toString() {
        return "Posn{" +
                "id= '" + id + "\'" +
                ", permissionLevel= '" + permissionLevel + "\'" +
                ", active= '" + active + "}";
    }

}