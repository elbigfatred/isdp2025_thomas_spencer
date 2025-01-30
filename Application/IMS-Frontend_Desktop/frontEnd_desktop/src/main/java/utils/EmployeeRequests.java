package utils;

import models.Employee;
import models.Posn;
import models.Site;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeRequests handles API interactions related to employee management.
 * It provides methods to fetch, add, update, and deactivate employees from the backend.
 *
 * Features:
 * - Fetch all employees or a specific employee by username.
 * - Add new employees with role and site assignments.
 * - Update existing employee details including roles and site.
 * - Deactivate employees securely.
 * - Uses REST API calls via `RestTemplate` and `HttpURLConnection`.
 */
public class EmployeeRequests {

    /**
     * Fetches a list of all employees from the backend API.
     * Parses the JSON response and maps it to Employee objects.
     *
     * @return A list of Employee objects, or an empty list if an error occurs.
     */
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
                employee.setActive(jsonObject.getInt("active") == 1);
                employee.setLocked(jsonObject.getInt("locked"));
                employee.setMainRole(jsonObject.getString("mainrole"));

                // Handle nested site object
                if (jsonObject.has("site")) {
                    JSONObject siteObject = jsonObject.getJSONObject("site");
                    Site site = new Site();
                    site.setId(siteObject.getInt("id"));
                    site.setSiteName(siteObject.getString("siteName"));
                    // Populate other Site fields as needed
                    employee.setSite(site);
                }

                // Handle roles array
                if (jsonObject.has("roles")) {
                    JSONArray rolesArray = jsonObject.getJSONArray("roles");
                    List<Posn> roles = new ArrayList<>();

                    for (int j = 0; j < rolesArray.length(); j++) {
                        JSONObject roleObject = rolesArray.getJSONObject(j).getJSONObject("posn");

                        Posn posn = new Posn();
                        posn.setId(roleObject.getInt("id"));
                        posn.setPermissionLevel(roleObject.getString("permissionLevel"));
                        posn.setActive(roleObject.getInt("active") == 1);

                        roles.add(posn);
                    }
                    employee.setRoles(roles);
                }

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }

    /**
     * Retrieves an employee by their username from the backend API.
     * If the employee does not exist, returns null.
     *
     * @param username The username of the employee to fetch.
     * @return An Employee object if found, otherwise null.
     */
    public static Employee fetchEmployeeByUsername(String username) {
        String endpoint = "http://localhost:8080/api/employees/username/" + username;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(endpoint, String.class);

            JSONObject jsonObject = new JSONObject(jsonResponse);

            Employee employee = new Employee();
            employee.setId(jsonObject.getInt("id"));
            employee.setPassword(jsonObject.getString("password"));
            employee.setFirstName(jsonObject.getString("firstname"));
            employee.setLastName(jsonObject.getString("lastname"));
            employee.setEmail(jsonObject.getString("email"));
            employee.setUsername(jsonObject.getString("username"));
            employee.setActive(jsonObject.getInt("active") == 1);
            employee.setLocked(jsonObject.getInt("locked"));
            employee.setMainRole(jsonObject.getString("mainrole"));

            // Handle roles array
            if (jsonObject.has("roles")) {
                JSONArray rolesArray = jsonObject.getJSONArray("roles");
                List<Posn> roles = new ArrayList<>();

                for (int i = 0; i < rolesArray.length(); i++) {
                    JSONObject roleObject = rolesArray.getJSONObject(i).getJSONObject("posn");

                    Posn posn = new Posn();
                    posn.setId(roleObject.getInt("id"));
                    posn.setPermissionLevel(roleObject.getString("permissionLevel"));
                    posn.setActive(roleObject.getInt("active") == 1);

                    roles.add(posn);
                }
                employee.setRoles(roles);
            }

            // Handle site object
            if (jsonObject.has("site")) {
                JSONObject siteObject = jsonObject.getJSONObject("site");
                Site site = new Site();
                site.setId(siteObject.getInt("id"));
                site.setSiteName(siteObject.getString("siteName"));
                employee.setSite(site);
            }

            return employee;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null; // Employee not found
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to the database.\nPlease try again later.");
        }
    }

    /**
     * Deactivates an employee by their ID using a PUT request.
     * Returns a status string indicating success, failure, or error.
     *
     * @param employeeId The ID of the employee to deactivate.
     * @return A status string: "success", "failure", "unexpected", or "network_error".
     */
    public static String deactivateEmployee(int employeeId) {
        String endpoint = "http://localhost:8080/api/employees/deactivate/" + employeeId;

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true); // Enable output for PUT requests

            // Send the request and get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return "success";
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return "failure";
            } else {
                return "unexpected";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "network_error";
        }
    }

    /**
     * Adds a new employee to the backend database using a POST request.
     * Converts the Employee object into JSON and sends it to the API.
     *
     * @param employee The Employee object to be added.
     * @return true if the employee was successfully added, false otherwise.
     */
    public static boolean addEmployee(Employee employee) {
        String endpoint = "http://localhost:8080/api/employees";

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Convert Employee object to JSON
            JSONObject employeeJson = new JSONObject();
            employeeJson.put("id", employee.getId());
            employeeJson.put("username", employee.getUsername());
            employeeJson.put("password", employee.getPassword());
            employeeJson.put("firstname", employee.getFirstName());
            employeeJson.put("lastname", employee.getLastName());
            employeeJson.put("email", employee.getEmail());
            employeeJson.put("active", employee.isActive());
            employeeJson.put("locked", employee.getLocked());
            employeeJson.put("mainrole", employee.getMainRole());

            // Add roles array
            JSONArray rolesArray = new JSONArray();
            for (Posn role : employee.getRoles()) {
                JSONObject roleJson = new JSONObject();
                roleJson.put("id", role.getId());
                rolesArray.put(roleJson);
            }
            employeeJson.put("roles", rolesArray);

            // Add site
            employeeJson.put("site", employee.getSite().getId());

            System.out.println(employeeJson.toString());

            // Send the JSON payload
            try (OutputStream os = connection.getOutputStream()) {
                os.write(employeeJson.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Get response
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_CREATED;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing employee’s details in the backend.
     * Sends a PUT request with the updated employee information.
     *
     * @param employee The Employee object containing updated details.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateEmployee(Employee employee) {
        String endpoint = "http://localhost:8080/api/employees/" + employee.getId();

        System.out.println(employee.toString());

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Convert Employee object to JSON
            JSONObject employeeJson = new JSONObject();
            employeeJson.put("firstname", employee.getFirstName());
            employeeJson.put("lastname", employee.getLastName());
            employeeJson.put("email", employee.getEmail());
            employeeJson.put("password", employee.getPassword()); // Include password only if changed
            employeeJson.put("active", employee.isActive());
            employeeJson.put("locked", employee.getLocked());
            employeeJson.put("mainrole", employee.getMainRole());


            // Add roles array
            JSONArray rolesArray = new JSONArray();
            for (Posn role : employee.getRoles()) {
                JSONObject roleJson = new JSONObject();
                roleJson.put("id", role.getId());
                rolesArray.put(roleJson);
            }
            employeeJson.put("roles", rolesArray);

            // Add site
            employeeJson.put("site", employee.getSite().getId());

            System.out.println("Update Payload: " + employeeJson.toString());

            // Send the JSON payload
            try (OutputStream os = connection.getOutputStream()) {
                os.write(employeeJson.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Get response
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK; // Return true if response is HTTP 200 OK

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
