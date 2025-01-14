package utils;

import models.Employee;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ReadEmployeesRequest {

    public static List<Employee> fetchEmployees() {
        String employeesEndpoint = "http://localhost:8080/api/employees";
        List<Employee> employees = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(employeesEndpoint, String.class);

            // Parse the JSON response
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Employee employee = new Employee();
                employee.setId(jsonObject.getInt("id"));
                employee.setFirstName(jsonObject.getString("firstname"));
                employee.setLastName(jsonObject.getString("lastname"));
                employee.setEmail(jsonObject.getString("email"));
                employee.setUsername(jsonObject.getString("username"));
                employee.setPermissionLevel(jsonObject.getJSONObject("posn").getString("permissionLevel"));
                employee.setActive(jsonObject.getInt("active") == 1);

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }

    // Fetch employee by username
    public static Employee fetchEmployeeByUsername(String username) {
        String endpoint = "http://localhost:8080/api/employees/username/" + username;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(endpoint, String.class);

            JSONObject jsonObject = new JSONObject(jsonResponse);

            Employee employee = new Employee();
            employee.setId(jsonObject.getInt("id"));
            employee.setFirstName(jsonObject.getString("firstname"));
            employee.setLastName(jsonObject.getString("lastname"));
            employee.setEmail(jsonObject.getString("email"));
            employee.setUsername(jsonObject.getString("username"));
            employee.setPermissionLevel(jsonObject.getJSONObject("posn").getString("permissionLevel"));
            employee.setActive(jsonObject.getInt("active") == 1);

            return employee;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // Username doesn't exist (404 from backend)
            return null;
        } catch (Exception e) {
            // Any other error (e.g., backend is down)
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to the backend. Please try again later.");
        }
    }}
