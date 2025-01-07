package tom.ims.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tom.ims.backend.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}