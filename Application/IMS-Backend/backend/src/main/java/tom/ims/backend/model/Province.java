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
@Table(name = "province")
public class Province {
    @Id
    @Column(name = "provinceID", nullable = false, length = 2)
    private String provinceID;

    @Column(name = "provinceName", nullable = false, length = 20)
    private String provinceName;

    @Column(name = "countryCode", nullable = false, length = 50)
    private String countryCode;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}