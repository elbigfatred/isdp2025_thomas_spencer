package utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


    public class ReadEmployeesRequest {


        /**
         * Sends a GET request to retrieve all employees and logs the response.
         *
         * @return The raw JSON response as a string, or an error message if the request fails.
         */
        public static String fetchEmployeesRaw() {
            String employeesEndpoint = "http://localhost:8080/api/employees"; // Replace with your backend URL

            try {
                // Create a URL object pointing to the employees API endpoint
                URL url = new URL(employeesEndpoint);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the HTTP request method to GET
                connection.setRequestMethod("GET");

                // Set headers (if necessary)
                connection.setRequestProperty("Accept", "application/json");

                // Get the response code
                int responseCode = connection.getResponseCode();

                // If the response code is HTTP 200 (OK), read and return the raw response
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream();
                         Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                        scanner.useDelimiter("\\A"); // Read the entire response
                        String jsonResponse = scanner.hasNext() ? scanner.next() : "";

                        // Log the response to the console
                        System.out.println("API Response:");
                        System.out.println(jsonResponse);

                        return jsonResponse; // Return the raw response as a string
                    }
                } else {
                    // Log the error response
                    System.err.println("Error: " + responseCode + " - Failed to fetch employees.");
                    return "Error: " + responseCode;
                }

            } catch (Exception e) {
                // Catch any exceptions and log them
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }
    }
