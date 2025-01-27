package utils;

import models.Employee;
import org.json.JSONObject;

public class AuthenticationService {
    public Employee fetchEmployee(String username) {
        // Wrap EmployeeRequests logic here
        return EmployeeRequests.fetchEmployeeByUsername(username);
    }

//    public JSONObject authenticate(String username, String password) {
//        // Wrap LoginRequests logic here
//        return LoginRequests.login(username, password);
//    }
}
