package tom.ims.backend.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.LoginRequest;
import tom.ims.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<Employee> getAllEmployees() throws JsonProcessingException {
        System.out.println(employeeService.getAllEmployees());
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable int id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Validate the login credentials
        Employee employee = employeeService.validateLogin(loginRequest.getUsername(), loginRequest.getPassword());

        if (employee != null) {
            // Return the employee data (excluding password)
            return ResponseEntity.ok(employee);
        } else {
            // Return a 401 Unauthorized status if credentials are invalid
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
