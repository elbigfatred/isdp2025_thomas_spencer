package utils;

import models.Employee;
import models.Posn;
import models.Site;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;
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
                // Handle nested posn object
                if (jsonObject.has("posn")) {
                    JSONObject posnObject = jsonObject.getJSONObject("posn");
                    Posn posn = new Posn();
                    posn.setId(posnObject.getInt("id"));
                    posn.setPermissionLevel(posnObject.getString("permissionLevel"));
                    posn.setActive(posnObject.getInt("active") == 1);
                    employee.setPermissionLevel(posn); // Assign the posn object
                }

                // Handle nested site object if required
                if (jsonObject.has("site")) {
                    JSONObject siteObject = jsonObject.getJSONObject("site");
                    Site site = new Site();
                    site.setId(siteObject.getInt("id"));
                    site.setSiteName(siteObject.getString("siteName"));
                    // Populate other Site fields as needed
                    employee.setSite(site); // Assign the site object
                }

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
            // Handle nested posn object
            if (jsonObject.has("posn")) {
                JSONObject posnObject = jsonObject.getJSONObject("posn");
                Posn posn = new Posn();
                posn.setId(posnObject.getInt("id"));
                posn.setPermissionLevel(posnObject.getString("permissionLevel"));
                posn.setActive(posnObject.getInt("active") == 1);
                employee.setPermissionLevel(posn); // Assign the posn object
            }

            // Handle nested site object if required
            if (jsonObject.has("site")) {
                JSONObject siteObject = jsonObject.getJSONObject("site");
                Site site = new Site();
                site.setId(siteObject.getInt("id"));
                site.setSiteName(siteObject.getString("siteName"));
                // Populate other Site fields as needed
                employee.setSite(site); // Assign the site object
            }            employee.setActive(jsonObject.getInt("active") == 1);

            return employee;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // Username doesn't exist (404 from backend)
            return null;
        } catch (Exception e) {
            // Any other error (e.g., backend is down)
            e.printStackTrace();
            throw new RuntimeException("Unable to connect to the backend. Please try again later.");
        }
    }


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
            employeeJson.put("permissionLevel", employee.getPermissionLevel().getId());
            employeeJson.put("site", employee.getSite().getId()); // Send only the site ID

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
}
