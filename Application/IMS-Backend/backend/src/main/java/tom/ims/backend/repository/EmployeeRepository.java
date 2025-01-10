package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tom.ims.backend.model.Employee;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByUsername(String username);
}