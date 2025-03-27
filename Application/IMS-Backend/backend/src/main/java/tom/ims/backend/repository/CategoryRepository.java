package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tom.ims.backend.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAll();

    Category findByCategoryName(String categoryName);
}