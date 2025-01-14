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

    public Employee findByUsername(String username) {
        // Attempt to find the employee by username
        return employeeRepository.findByUsername(username).orElse(null);
    }

//    TODO: get encryption going
    public Employee validateLogin(String username, String password) {
        try {
            // Find employee by username
            Employee employee = findByUsername(username);

            // Check if the password matches (consider using a secure hashing mechanism in production)
            if (employee.getPassword().equals(password)) {
                return employee; // Return the employee if credentials are valid
            } else {
                System.out.println("Invalid password for username: " + username);
                return null; // Invalid credentials
            }
        } catch (RuntimeException e) {
            // Handle case where the username does not exist
            System.out.println("Error during login validation: " + e.getMessage());
            return null; // Invalid credentials
        }
    }

    public boolean validatePassword(Employee employee, String password) {
        // Replace this with secure password hashing logic (e.g., BCrypt)
        return employee.getPassword().equals(password);
    }

    public void saveEmployee(Employee employee) {
        try {
            // Save the employee to the database using the repository
            employeeRepository.save(employee);
            System.out.println("Employee saved successfully: " + employee);
        } catch (Exception e) {
            // Log any exceptions that occur during the save process
            System.err.println("Error saving employee: " + employee);
            e.printStackTrace();

            // Rethrow the exception to indicate failure if necessary
            throw new RuntimeException("Failed to save employee: " + e.getMessage(), e);
        }
    }
}