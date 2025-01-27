package tom.ims.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.LoginRequest;
import tom.ims.backend.service.EmployeeService;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final String defaultPassword = "P@ssw0rd-";

    @Autowired
    private EmployeeService employeeService;

    // for tracking failed logins
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();

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
                resetFailedAttempts(employee.getUsername());
                System.out.println("Account is locked for user: " + loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Your account has been locked due to too many incorrect login attempts. " +
                                "Please contact your Administrator at admin@bullseye.ca for assistance.");
            }

            // Check if the stored password is the default
            if ("P@ssw0rd-".equals(employee.getPassword()) && defaultPassword.equals(loginRequest.getPassword())) {
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
                    resetFailedAttempts(employee.getUsername());
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
