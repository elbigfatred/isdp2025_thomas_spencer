package utils;

import org.json.JSONObject;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LoginRequests {

//    private static final int HTTP_PRECONDITION_REQUIRED = 428; // Standard HTTP status code
//
//    public static JSONObject login(String username, String password) throws Exception {
//        String loginEndpoint = "http://localhost:8080/api/login";
//
//        URL url = new URL(loginEndpoint);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setDoOutput(true);
//
//        // Create the JSON payload
//        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
//        System.out.println("Sending POST request to: " + loginEndpoint);
//        System.out.println("Request payload: " + payload);
//
//        // Write payload to the connection
//        try (OutputStream os = connection.getOutputStream()) {
//            os.write(payload.getBytes(StandardCharsets.UTF_8));
//        }
//
//        // Get the response code
//        int responseCode = connection.getResponseCode();
//        System.out.println("Response Code: " + responseCode);
//
//        // Handle HTTP_OK (200)
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
//                scanner.useDelimiter("\\A");
//                String response = scanner.hasNext() ? scanner.next() : "";
//                System.out.println("Response body: " + response);
//                return new JSONObject(response); // Successful login, return JSON response
//            }
//        }
//
//        // Handle HTTP_PRECONDITION_REQUIRED (428) for password change
//        if (responseCode == HTTP_PRECONDITION_REQUIRED) {
//            try (java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream(), StandardCharsets.UTF_8)) {
//                scanner.useDelimiter("\\A");
//                String response = scanner.hasNext() ? scanner.next() : "";
//                System.out.println("Response body: " + response);
//                throw new Exception(response);
//            }
//        }
//
//        // Handle HTTP_UNAUTHORIZED (401) for invalid credentials or account issues
//        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
//            try (java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream(), StandardCharsets.UTF_8)) {
//                scanner.useDelimiter("\\A");
//                String response = scanner.hasNext() ? scanner.next() : "";
//                System.out.println("Error response body: " + response);
//                throw new Exception(response); // Pass the error message back to the caller
//            }
//        }
//
//        // Handle unexpected responses
//        throw new Exception("Unexpected error occurred: HTTP " + responseCode);
//    }
//
//    public static JSONObject resetPassword(String username, String newPassword) throws Exception {
//        String resetEndpoint = "http://localhost:8080/api/employees/reset-password";
//
//        URL url = new URL(resetEndpoint);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setDoOutput(true);
//
//        // Create JSON payload
//        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, newPassword);
//
//        // Send payload
//        try (OutputStream os = connection.getOutputStream()) {
//            os.write(payload.getBytes(StandardCharsets.UTF_8));
//        }
//
//        int responseCode = connection.getResponseCode();
//
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
//                scanner.useDelimiter("\\A");
//                return new JSONObject(scanner.hasNext() ? scanner.next() : "");
//            }
//        } else {
//            try (java.util.Scanner scanner = new java.util.Scanner(connection.getErrorStream(), StandardCharsets.UTF_8)) {
//                scanner.useDelimiter("\\A");
//                throw new Exception(scanner.hasNext() ? scanner.next() : "Failed to reset password");
//            }
//        }
//    }

    private static final String BASE_URL = "http://localhost:8080/api";

    private static HttpURLConnection setupConnection(String endpoint, String method, String contentType) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true); // Required for POST/PUT
        return connection;
    }

    private static String sendRequest(HttpURLConnection connection, String payload) throws Exception {
        // Write payload if provided
        if (payload != null && !payload.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
        }

        // Read the response
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) { // Success
            return readResponse(connection.getInputStream());
        } else { // Error
            throw new Exception(readResponse(connection.getErrorStream()));
        }
    }

    private static String readResponse(java.io.InputStream inputStream) throws Exception {
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static JSONObject login(String username, String password) throws Exception {
        String endpoint = "/login";
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        HttpURLConnection connection = setupConnection(endpoint, "POST", "application/json");
        String response = sendRequest(connection, payload);

        return new JSONObject(response); // Return parsed JSON
    }

    public static JSONObject resetPassword(String username, String newPassword) throws Exception {
        String endpoint = "/employees/reset-password";
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, newPassword);

        HttpURLConnection connection = setupConnection(endpoint, "POST", "application/json");
        String response = sendRequest(connection, payload);

        return new JSONObject(response); // Return parsed JSON
    }
}