package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Site;

public interface SiteRepository extends JpaRepository<Site, Integer> {

}