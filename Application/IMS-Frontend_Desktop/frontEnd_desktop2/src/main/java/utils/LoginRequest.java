package utils;

import org.json.JSONObject;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginRequest {

    private static final int HTTP_PRECONDITION_REQUIRED = 428; // Standard HTTP status code

    public static JSONObject login(String username, String password) throws Exception {
        String loginEndpoint = "http://localhost:8080/api/employees/login";

        URL url = new URL(loginEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create the JSON payload
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        System.out.println("Sending POST request to: " + loginEndpoint);
        System.out.println("Request payload: " + payload);

        // Write payload to the connection
        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        // Get the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Handle HTTP_OK (200)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                System.out.println("Response body: " + response);
                return new JSONObject(response); // Successful login, return JSON response
            }
        }

        // Handle HTTP_PRECONDITION_REQUIRED (428) for password change
        if (responseCode == HTTP_PRECONDITION_REQUIRED) {
            try (java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream(), StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                System.out.println("Response body: " + response);
                throw new Exception(response);
            }
        }

        // Handle HTTP_UNAUTHORIZED (401) for invalid credentials or account issues
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            try (java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream(), StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                System.out.println("Error response body: " + response);
                throw new Exception(response); // Pass the error message back to the caller
            }
        }

        // Handle unexpected responses
        throw new Exception("Unexpected error occurred: HTTP " + responseCode);
    }

    public static JSONObject resetPassword(String username, String newPassword) {
        String resetEndpoint = "http://localhost:8080/api/employees/reset-password";

        try {
            URL url = new URL(resetEndpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create JSON payload
            String payload = String.format("{\"username\":\"%s\",\"newPassword\":\"%s\"}", username, newPassword);

            // Send the payload
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    scanner.useDelimiter("\\A");
                    return new JSONObject(scanner.hasNext() ? scanner.next() : "");
                }
            } else {
                throw new Exception("Failed to reset password. HTTP Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}