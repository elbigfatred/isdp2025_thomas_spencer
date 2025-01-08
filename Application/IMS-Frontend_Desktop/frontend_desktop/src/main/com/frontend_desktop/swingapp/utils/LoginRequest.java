package main.java.com.frontend_desktop.swingapp.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginRequest {
    /**
     * Sends a POST request to the backend login API with username and password.
     *
     * @param username The username to authenticate with.
     * @param password The password to authenticate with.
     * @return The response from the API as a string if successful, or an error/exception message.
     */
    public static String login(String username, String password) {
        String loginEndpoint = "http://localhost:8080/api/employees/login"; // URL of the backend login endpoint

        try {
            // Create a URL object pointing to the login API endpoint
            URL url = new URL(loginEndpoint);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the HTTP request method to POST
            connection.setRequestMethod("POST");

            // Specify that the request body will contain JSON data
            connection.setRequestProperty("Content-Type", "application/json");

            // Enable output mode to allow writing data to the connection
            connection.setDoOutput(true);

            // Create the JSON payload (body) to send in the POST request
            String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

            /**
             * Write the payload to the output stream of the connection.
             *
             * Explanation:
             * - The `OutputStream` is used to write the JSON payload to the body of the POST request.
             * - This allows the backend to receive the username and password as part of the request body.
             * - Data is encoded in UTF-8 to support special characters in usernames and passwords.
             */
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8); // Convert the JSON string to bytes
                os.write(input, 0, input.length); // Write the byte array to the output stream
            }

            // Get the response code from the server (e.g., 200 for success, 401 for unauthorized)
            int responseCode = connection.getResponseCode();

            // If the response code is HTTP 200 (OK), read the response body
            if (responseCode == HttpURLConnection.HTTP_OK) {
                /**
                 * Read the response from the server using an InputStream.
                 *
                 * Explanation:
                 * - The response is read using a Scanner to process the data stream.
                 * - The `useDelimiter("\\A")` trick reads the entire response into a single string.
                 */
                try (java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    scanner.useDelimiter("\\A"); // Delimiter "\\A" means "read everything until the end of the stream"
                    return scanner.hasNext() ? scanner.next() : ""; // Return the response as a string
                }
            } else {
                // If the response code is not 200, return an error message with the code
                return "Error: " + responseCode;
            }

        } catch (Exception e) {
            // Catch any exceptions (e.g., network issues, malformed JSON, etc.) and return the error message
            e.printStackTrace(); // Print the exception stack trace for debugging
            return "Exception: " + e.getMessage();
        }
    }
}
