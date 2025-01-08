package tom.ims.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import tom.ims.backend.model.Employee;
import tom.ims.backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(int id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found!"));
    }

    public Employee validateLogin(String username, String password) {
        // Find employee by username
        Employee employee = employeeRepository.findByUsername(username);

        if (employee != null && employee.getPassword().equals(password)) {
            // Check if the password matches (you should use hashed passwords in production)
            return employee;
        }

        return null; // Invalid credentials
    }
}