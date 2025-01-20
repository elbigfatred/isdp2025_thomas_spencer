package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
}
