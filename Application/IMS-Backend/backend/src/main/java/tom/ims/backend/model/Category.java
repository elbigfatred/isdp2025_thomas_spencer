package tom.ims.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Data
@Table(name = "category")
public class Category {
    @Id
    @Column(name = "categoryName", nullable = false, length = 32)
    private String categoryName;

    @ColumnDefault("1")
    @Column(name = "active", nullable = false)
    private Byte active;

}