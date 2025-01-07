package tom.ims.backend.model;

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
    private Integer id;

    @Column(name = "permissionLevel", nullable = false, length = 20)
    private String permissionLevel;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}