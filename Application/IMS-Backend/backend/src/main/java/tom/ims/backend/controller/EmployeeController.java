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
                newPassword = defaultPassword; // Default password
            }
            //String hashedPassword = HashUtil.hashPassword(newPassword);
            employee.setPassword(newPassword);

            employee.setFirstName((String) employeeData.get("firstname"));
            employee.setLastName((String) employeeData.get("lastname"));
            employee.setEmail((String) employeeData.get("email"));
            employee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
            employee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));

            // Fetch and set the Site
            Integer siteId = (Integer) employeeData.get("site");
            Site site = siteService.getSiteById(siteId);
            employee.setSite(site);

            employee.setMainRole((String) employeeData.get("mainrole"));

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


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployee(@PathVariable int id, @RequestBody Map<String, Object> employeeData) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
            }

            // Update basic fields (as before)
            //            // Update basic fields
            employee.setFirstName((String) employeeData.get("firstname"));
            employee.setLastName((String) employeeData.get("lastname"));
            employee.setEmail((String) employeeData.get("email"));
            employee.setActive((Byte) ((Boolean) employeeData.get("active") ? (byte) 1 : (byte) 0));
            employee.setLocked((Byte) ((Boolean) employeeData.get("locked") ? (byte) 1 : (byte) 0));
            employee.setMainRole((String) employeeData.get("mainrole"));


            // Update password if provided
            if (employeeData.containsKey("password")) {
                String newPassword = (String) employeeData.get("password");
                if (newPassword != null && !newPassword.isEmpty()) {
                    String hashedPassword = HashUtil.hashPassword(newPassword);
                    employee.setPassword(hashedPassword);
                }
            }

            // Update site
            Integer siteId = (Integer) employeeData.get("site");
            if (siteId != null) {
                Site site = siteService.getSiteById(siteId);
                employee.setSite(site);
            }

            // Handle roles
            if (employeeData.containsKey("roles")) {
                System.out.println(" --- MODIFYING ROLES --- ");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rolesData = (List<Map<String, Object>>) employeeData.get("roles");

                // Convert rolesData into a list of UserPosn
                List<UserPosn> newRoles = rolesData.stream().map(role -> {
                    Integer posnId = (Integer) role.get("id");
                    Posn posn = posnService.getPositionById(posnId);

                    UserPosn userPosn = new UserPosn();
                    userPosn.setId(new UserPosnKey(employee.getId(), posnId));
                    userPosn.setUser(employee);
                    userPosn.setPosn(posn);

                    return userPosn;
                }).collect(Collectors.toList());

                userPosnService.replaceRolesForEmployee(id, newRoles);
            }

            employeeService.saveEmployee(employee); // Save employee

            return ResponseEntity.ok("Employee updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
