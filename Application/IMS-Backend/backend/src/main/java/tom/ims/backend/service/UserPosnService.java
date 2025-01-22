package tom.ims.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.Posn;
import tom.ims.backend.model.UserPosn;
import tom.ims.backend.model.UserPosnKey;
import tom.ims.backend.repository.EmployeeRepository;
import tom.ims.backend.repository.PosnRepository;
import tom.ims.backend.repository.UserPosnRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserPosnService {

    @Autowired
    private UserPosnRepository userPosnRepository;

    @Autowired
    private PosnRepository posnRepository;

    @Autowired
    private EmployeeService employeeService ;

    @PersistenceContext
    private EntityManager entityManager;


    // Save a single UserPosn
    @Transactional
    public void saveUserPosn(UserPosn userPosn) {
        if (userPosn.getId() == null) {
            UserPosnKey key = new UserPosnKey();
            key.setUserID(userPosn.getUser().getId());
            key.setPosnID(userPosn.getPosn().getId());
            userPosn.setId(key);
        }

        System.out.println("Preparing to save UserPosn: " + userPosn);

        if (userPosn.getUser() == null || userPosn.getPosn() == null) {
            throw new IllegalArgumentException("User or Position cannot be null.");
        }

        userPosnRepository.save(userPosn);

        System.out.println("Saved UserPosn: " + userPosn);
    }

    // Find UserPosns by employee ID
    public List<UserPosn> getUserPosnsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        return userPosnRepository.findByUserId(userId);
    }

    // Delete all UserPosns by Employee
    @Transactional
    public void deleteRolesByEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee or Employee ID cannot be null.");
        }
        userPosnRepository.deleteByUser(employee);
        System.out.println("Deleted all roles for employee: " + employee.getId());
    }

    @Transactional
    public void replaceRolesForEmployee(int employeeId, List<UserPosn> newRoles) {
        // Fetch the employee
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found.");
        }

        // Fetch existing roles
        List<UserPosn> currentRoles = userPosnRepository.findByUser(employee);
        System.out.println("Current roles: " + currentRoles);

        // Fetch all possible roles (for debugging purposes, optional)
        List<Posn> allRoles = posnRepository.findAll();
        System.out.println("All possible roles: " + allRoles);

        // Determine roles to delete
        Set<Integer> newRoleIds = newRoles.stream()
                .map(userPosn -> userPosn.getPosn().getId())
                .collect(Collectors.toSet());

        List<UserPosn> rolesToDelete = currentRoles.stream()
                .filter(currentRole -> !newRoleIds.contains(currentRole.getPosn().getId()))
                .collect(Collectors.toList());

        // Determine roles to add
        Set<Integer> currentRoleIds = currentRoles.stream()
                .map(userPosn -> userPosn.getPosn().getId())
                .collect(Collectors.toSet());

        List<UserPosn> rolesToAdd = newRoles.stream()
                .filter(newRole -> !currentRoleIds.contains(newRole.getPosn().getId()))
                .collect(Collectors.toList());

        // Delete roles in batch
        if (!rolesToDelete.isEmpty()) {
            userPosnRepository.deleteAllInBatch(rolesToDelete);
            userPosnRepository.flush(); // Ensures changes are applied immediately
            System.out.println("Deleted roles in batch: " + rolesToDelete);
        }

        // Add new roles in batch
        if (!rolesToAdd.isEmpty()) {
            userPosnRepository.saveAll(rolesToAdd);
            userPosnRepository.flush(); // Ensures changes are applied immediately
            System.out.println("Added roles in batch: " + rolesToAdd);
        }

        System.out.println("Roles successfully replaced for employee: " + employee.getId());
    }




    public void deleteUserPosnById(UserPosnKey id) {
        if (id == null) {
            throw new IllegalArgumentException("UserPosnKey cannot be null.");
        }
        try {
            // Fetch the UserPosn entity
            Optional<UserPosn> userPosnOptional = userPosnRepository.findById(id);
            if (userPosnOptional.isPresent()) {
                UserPosn userPosn = userPosnOptional.get();
                userPosnRepository.delete(userPosn); // Remove the entity
                userPosnRepository.flush(); // Force immediate execution of the delete
                System.out.println("Deleted UserPosn with ID: " + id);
            } else {
                System.out.println("No UserPosn found with ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error deleting UserPosn with ID: " + id + " - " + e.getMessage());
            throw new RuntimeException("Error deleting UserPosn", e);
        }
    }

    @Modifying
    @Query("DELETE FROM UserPosn up WHERE up.id.userID = :userId AND up.id.posnID = :posnId")
    public void hardDeleteUserPosnById(@Param("userId") Integer userId, @Param("posnId") Integer posnId){};

    public void flush() {
        userPosnRepository.flush();
    }
}