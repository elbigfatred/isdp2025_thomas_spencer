package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "txntype")
public class Txntype {
    @Id
    @Column(name = "txnType", nullable = false, length = 20)
    private String txnType;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}