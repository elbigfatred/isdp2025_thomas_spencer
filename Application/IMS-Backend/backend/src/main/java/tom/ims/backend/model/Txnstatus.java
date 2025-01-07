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
@Table(name = "txnstatus")
public class Txnstatus {
    @Id
    @Column(name = "statusName", nullable = false, length = 20)
    private String statusName;

    @Column(name = "statusDescription", nullable = false, length = 100)
    private String statusDescription;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}