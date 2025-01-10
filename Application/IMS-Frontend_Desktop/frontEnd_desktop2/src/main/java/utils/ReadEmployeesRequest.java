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

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employees;
    }
}
