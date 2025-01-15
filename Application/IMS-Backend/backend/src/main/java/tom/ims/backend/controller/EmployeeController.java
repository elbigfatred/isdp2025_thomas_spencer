package tom.ims.backend.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tom.ims.backend.model.*;
import tom.ims.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.service.PosnService;
import tom.ims.backend.service.SiteService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    // for tracking failed logins
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private PosnService posnService;

    @GetMapping
    public List<Employee> getAllEmployees() throws JsonProcessingException {
        //System.out.println(employeeService.getAllEmployees());
        return employeeService.getAllEmployees();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getEmployeeByUsername(@PathVariable String username) {
        try {
            Employee employee = employeeService.findByUsername(username);

            if (employee == null) {
                // Return 404 Not Found if the employee doesn't exist
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }

            // Return the employee if found
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            // Handle unexpected errors (e.g., DB connectivity issues)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable int id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login request received:");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("Password: " + loginRequest.getPassword());

        try {
            // Fetch the employee by username
            Employee employee;
            try {
                employee = employeeService.findByUsername(loginRequest.getUsername());
            } catch (RuntimeException e) {
                // Employee does not exist; we don't increment failed attempts in this case
                System.out.println("Invalid login attempt: Username does not exist - " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }

            // Check if the account is locked
            if (employee.getLocked() == 1) {
                System.out.println("Account is locked for user: " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Your account has been locked due to too many incorrect login attempts. " +
                                "Please contact your Administrator at admin@bullseye.ca for assistance.");
            }

            // Check if the stored password is the default
            if ("P@ssw0rd-".equals(employee.getPassword()) && "P@ssw0rd-".equals(loginRequest.getPassword())) {
                resetFailedAttempts(employee.getUsername());
                System.out.println("Default password detected. Prompting user to change password.");
                return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                        .body("Password change required. Please update your password.");
            }

            // Validate the hashed password
            if (!HashUtil.matchesPassword(loginRequest.getPassword(), employee.getPassword())) {
                System.out.println("Invalid password for username: " + loginRequest.getUsername());

                // Increment failed login attempts
                incrementFailedAttempts(employee.getUsername());

                // Check if the account should be locked
                if (failedLoginAttempts.getOrDefault(employee.getUsername(), 0) >= 3) {
                    employee.setLocked((byte) 1); // Lock the account
                    employeeService.saveEmployee(employee); // Save the locked status to DB
                    System.out.println("Account locked due to too many failed attempts: " + loginRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Your account has been locked due to too many incorrect login attempts. Please contact your Administrator at admin@bullseye.ca for assistance.");
                }

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }

            // Reset failed attempts on successful login
            resetFailedAttempts(employee.getUsername());

            // Check if the account is active
            if (employee.getActive() == 0) {
                System.out.println("Account is not active for user: " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username and/or password. Please contact your Administrator at admin@bullseye.ca for assistance.");
            }

            // Exclude sensitive fields like the password before sending the response
            employee.setPassword(null);

            System.out.println("Login successful for user: " + employee.getUsername());
            return ResponseEntity.ok(employee);

        } catch (Exception e) {
            System.err.println("Error during login process:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Employee employee = employeeService.findByUsername(request.getUsername());
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Hash the new password
            String hashedPassword = HashUtil.hashPassword(request.getPassword());

            // Update the password in the database
            employee.setPassword(hashedPassword);
            employeeService.saveEmployee(employee);

            return ResponseEntity.ok(Collections.singletonMap("status", "success"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
        }
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<String> deactivateEmployee(@PathVariable int id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);

            // Check if the employee is already inactive
            if (employee.getActive() == 0) {
                System.out.println("Account is not active for user: " + employee.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee is already inactive.");
            }

            // Deactivate the employee
            employee.setActive((byte) 0);
            employeeService.saveEmployee(employee); // Save the updated employee status to the database

            System.out.println("Employee deactivated: " + employee.getUsername());
            return ResponseEntity.ok("Employee deactivated successfully.");
        } catch (RuntimeException e) {
            System.err.println("Error deactivating employee: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to deactivate employee.");
        } catch (Exception e) {
            System.err.println("Unexpected error occurred while deactivating employee:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmployee(@RequestBody Map<String, Object> employeeData) {
        try {

            System.out.println(employeeData);
            Employee employee = new Employee();
            employee.setId((Integer) employeeData.get("id"));
            employee.setUsername((String) employeeData.get("username"));
            employee.setPassword((String) employeeData.get("password"));
            employee.setFirstName((String) employeeData.get("firstname"));
            employee.setLastName((String) employeeData.get("lastname"));
            employee.setEmail((String) employeeData.get("email"));
            employee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
            employee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));

            // Fetch the Site using the provided site ID
            Integer siteId = (Integer) employeeData.get("site");
            Site site = siteService.getSiteById(siteId);
            employee.setSite(site);

            // Fetch the Posn using the provided position ID
            Integer posnId = (Integer) employeeData.get("permissionLevel");
            Posn posn = posnService.getPositionById(posnId);
            employee.setPosn(posn);

            // Save the employee
            employeeService.saveEmployee(employee);
        } catch (Exception e) {
            throw new RuntimeException("Error saving employee: " + e.getMessage());
        }
    }

    // Increment failed login attempts
    private void incrementFailedAttempts(String username) {
        failedLoginAttempts.put(username, failedLoginAttempts.getOrDefault(username, 0) + 1);
        System.out.println("Failed login attempts for " + username + ": " + failedLoginAttempts.get(username));
    }

    // Reset failed login attempts
    private void resetFailedAttempts(String username) {
        failedLoginAttempts.remove(username);
        System.out.println("Failed login attempts reset for " + username);
    }
}
