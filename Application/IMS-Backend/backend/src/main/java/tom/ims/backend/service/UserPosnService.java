package tom.ims.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tom.ims.backend.model.Employee;
import tom.ims.backend.model.UserPosn;
import tom.ims.backend.model.UserPosnKey;
import tom.ims.backend.repository.UserPosnRepository;

import java.util.List;

@Service
public class UserPosnService {

    @Autowired
    private UserPosnRepository userPosnRepository;

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

    // Replace all UserPosns for an Employee
    @Transactional
    public void replaceRolesForEmployee(Employee employee, List<UserPosn> newRoles) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee or Employee ID cannot be null.");
        }

        // Delete existing roles
        userPosnRepository.deleteByUser(employee);
        System.out.println("Deleted all roles for employee: " + employee.getId());

        // Save new roles
        for (UserPosn userPosn : newRoles) {
            userPosnRepository.save(userPosn);
            System.out.println("Saved role: " + userPosn);
        }
    }

    // Delete a UserPosn by its composite key
    @Transactional
    public void deleteUserPosnById(UserPosnKey id) {
        if (id == null) {
            throw new IllegalArgumentException("UserPosnKey cannot be null.");
        }
        try {
            userPosnRepository.deleteById(id);
            System.out.println("Deleted UserPosn with ID: " + id);
        } catch (Exception e) {
            System.err.println("Error deleting UserPosn with ID: " + id + " - " + e.getMessage());
            throw new RuntimeException("Error deleting UserPosn", e);
        }
    }
}