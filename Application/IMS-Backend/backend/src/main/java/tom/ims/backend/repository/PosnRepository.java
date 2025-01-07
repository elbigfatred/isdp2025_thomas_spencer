package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Posn;

public interface PosnRepository extends JpaRepository<Posn, Integer> {
}