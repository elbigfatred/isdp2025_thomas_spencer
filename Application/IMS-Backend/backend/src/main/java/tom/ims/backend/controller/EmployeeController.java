package tom.ims.backend.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tom.ims.backend.model.*;
import tom.ims.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tom.ims.backend.service.PosnService;
import tom.ims.backend.service.SiteService;
import tom.ims.backend.service.UserPosnService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final String defaultPassword = "P@ssw0rd-";


    // for tracking failed logins
    private final ConcurrentHashMap<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private UserPosnService userPosnService;

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
                resetFailedAttempts(employee.getUsername());
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

            // Create Employee object
            Employee employee = new Employee();
            employee.setId((Integer) employeeData.get("id"));
            employee.setUsername((String) employeeData.get("username"));

            // Handle password (set to default if blank or null)
            String newPassword = (String) employeeData.get("password");
            if (newPassword == null || newPassword.isEmpty()) {
                newPassword = "P@ssw0rd-"; // Default password
            }
            String hashedPassword = HashUtil.hashPassword(newPassword);
            employee.setPassword(hashedPassword);

            employee.setFirstName((String) employeeData.get("firstname"));
            employee.setLastName((String) employeeData.get("lastname"));
            employee.setEmail((String) employeeData.get("email"));
            employee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
            employee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));

            // Fetch and set the Site
            Integer siteId = (Integer) employeeData.get("site");
            Site site = siteService.getSiteById(siteId);
            employee.setSite(site);

            // Save the Employee first (to ensure the ID exists for UserPosn)
            employeeService.saveEmployee(employee);

            // Process roles and create UserPosn mappings
            List<Map<String, Object>> roles = (List<Map<String, Object>>) employeeData.get("roles");
            if (roles != null && !roles.isEmpty()) {
                for (Map<String, Object> roleData : roles) {
                    Integer posnId = (Integer) roleData.get("id"); // Extract role ID

                    // Create UserPosn object
                    UserPosn userPosn = new UserPosn();
                    userPosn.setId(new UserPosnKey(employee.getId(), posnId));
                    userPosn.setUser(employee);
                    Posn posn = posnService.getPositionById(posnId);
                    userPosn.setPosn(posn);

                    // Save the UserPosn mapping
                    userPosnService.saveUserPosn(userPosn);
                }
            }

            System.out.println("Employee saved successfully: " + employee);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving employee: " + e.getMessage());
        }
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateEmployee(@PathVariable int id, @RequestBody Map<String, Object> employeeData) {
//        try {
//            // Fetch the employee to update
//            Employee existingEmployee = employeeService.getEmployeeById(id);
//
//            if (existingEmployee == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
//            }
//
//            // Update employee fields
//            existingEmployee.setFirstName((String) employeeData.get("firstname"));
//            existingEmployee.setLastName((String) employeeData.get("lastname"));
//            existingEmployee.setEmail((String) employeeData.get("email"));
//            existingEmployee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
//            existingEmployee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));
//
//            // If password is provided, hash it and update
//            if (employeeData.containsKey("password")) {
//                String newPassword = (String) employeeData.get("password");
//                if (!newPassword.isEmpty()) {
//                    String hashedPassword = HashUtil.hashPassword(newPassword);
//                    existingEmployee.setPassword(hashedPassword);
//                }
//            }
//
//            // Fetch and update the Site
//            Integer siteId = (Integer) employeeData.get("site");
//            Site site = siteService.getSiteById(siteId);
//            existingEmployee.setSite(site);
//
//            // Fetch and update the Position
//            Integer posnId = (Integer) employeeData.get("permissionLevel");
//            Posn posn = posnService.getPositionById(posnId);
//            //existingEmployee.setPosn(posn); TODO
//
//            // Save the updated employee
//            employeeService.saveEmployee(existingEmployee);
//
//            return ResponseEntity.ok("Employee updated successfully.");
//        } catch (RuntimeException e) {
//            System.err.println("Error updating employee: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update employee.");
//        } catch (Exception e) {
//            System.err.println("Unexpected error occurred while updating employee:");
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
//        }
//    }

    @PutMapping("/{id}")
    @Transactional // Ensures atomicity and proper database interaction
    public ResponseEntity<?> updateEmployee(@PathVariable int id, @RequestBody Map<String, Object> employeeData) {
        try {
            // Fetch the existing employee
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
            }

            // Update basic fields
            existingEmployee.setFirstName((String) employeeData.get("firstname"));
            existingEmployee.setLastName((String) employeeData.get("lastname"));
            existingEmployee.setEmail((String) employeeData.get("email"));
            existingEmployee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
            existingEmployee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));

            // Update password if provided
            if (employeeData.containsKey("password")) {
                String newPassword = (String) employeeData.get("password");
                if (newPassword != null && !newPassword.isEmpty()) {
                    String hashedPassword = HashUtil.hashPassword(newPassword);
                    existingEmployee.setPassword(hashedPassword);
                }
            }

            // Update site
            Integer siteId = (Integer) employeeData.get("site");
            if (siteId != null) {
                Site site = siteService.getSiteById(siteId);
                existingEmployee.setSite(site);
            }

            // Handle roles
            if (employeeData.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rolesData = (List<Map<String, Object>>) employeeData.get("roles");

                System.out.println("Roles received for update: " + rolesData);

                // Fetch existing roles for the employee
                List<UserPosn> existingRoles = userPosnService.getUserPosnsByUserId(existingEmployee.getId());
                System.out.println("Existing roles: " + existingRoles);

                // Convert incoming roles to a Set of IDs for easier comparison
                Set<Integer> incomingRoleIds = rolesData.stream()
                        .map(role -> (Integer) role.get("id"))
                        .collect(Collectors.toSet());

                // Iterate through existing roles and delete those not in the incoming list
                for (UserPosn existingRole : existingRoles) {
                    if (!incomingRoleIds.contains(existingRole.getPosn().getId())) {
                        System.out.println("Deleting role: " + existingRole.getPosn().getPermissionLevel());
                        userPosnService.deleteUserPosnById(existingRole.getId());
                    } else {
                        System.out.println("Keeping role: " + existingRole.getPosn().getPermissionLevel());
                    }
                }

                // Iterate through incoming roles and add any that don't already exist
                for (Map<String, Object> roleData : rolesData) {
                    Integer posnId = (Integer) roleData.get("id");
                    boolean alreadyExists = existingRoles.stream()
                            .anyMatch(existingRole -> existingRole.getPosn().getId().equals(posnId));

                    if (!alreadyExists) {
                        Posn posn = posnService.getPositionById(posnId);

                        // Create and save the new UserPosn
                        UserPosn userPosn = new UserPosn();
                        userPosn.setId(new UserPosnKey(existingEmployee.getId(), posnId));
                        userPosn.setUser(existingEmployee);
                        userPosn.setPosn(posn);

                        System.out.println("Adding new role: " + posn.getPermissionLevel());
                        userPosnService.saveUserPosn(userPosn);
                    }
                }
            }

            // Save the updated employee
            employeeService.saveEmployee(existingEmployee);

            return ResponseEntity.ok("Employee updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the employee.");
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
