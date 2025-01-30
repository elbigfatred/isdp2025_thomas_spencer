package utils;

import models.Posn;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * PositionRequests handles API interactions for retrieving position data.
 *
 * Features:
 * - Fetch all positions from the backend API.
 * - Parses JSON response and maps it to Posn objects.
 * - Uses `HttpURLConnection` for API communication.
 */
public class PositionRequests {

    private static final String POSITIONS_ENDPOINT = "http://localhost:8080/api/positions";

    /**
     * Fetches a list of all positions from the backend API.
     * Parses JSON data and maps it to Posn objects.
     *
     * @return A list of Posn objects, or an empty list if an error occurs.
     */
    public static List<Posn> fetchPositions() {
        List<Posn> positions = new ArrayList<>();

        try {
            // Open connection
            URL url = new URL(POSITIONS_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read and parse the response
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    scanner.useDelimiter("\\A");
                    String jsonResponse = scanner.hasNext() ? scanner.next() : "";
                    JSONArray jsonArray = new JSONArray(jsonResponse);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Posn posn = new Posn();
                        posn.setId(jsonObject.getInt("id"));
                        posn.setPermissionLevel(jsonObject.getString("permissionLevel"));
                        posn.setActive(jsonObject.getInt("active") == 1);

                        positions.add(posn);
                    }
                }
            } else {
                System.err.println("Failed to fetch positions. HTTP Code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return positions;
    }
}