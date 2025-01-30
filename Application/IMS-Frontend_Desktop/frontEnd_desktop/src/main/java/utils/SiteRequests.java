package utils;

import models.Site;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * SiteRequests handles API interactions for managing site locations.
 *
 * Features:
 * - Fetch all available sites from the backend.
 * - Parses site details including address, province, and contact information.
 * - Uses `HttpURLConnection` to perform API requests.
 */
public class SiteRequests {

    /**
     * Fetches a list of all active sites from the backend API.
     * Parses JSON data and maps it to Site objects with detailed attributes.
     *
     * @return A list of Site objects, or an empty list if an error occurs.
     */
    public static List<Site> fetchSites() {
        String endpoint = "http://localhost:8080/api/sites";
        List<Site> sites = new ArrayList<>();

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    scanner.useDelimiter("\\A");
                    String jsonResponse = scanner.hasNext() ? scanner.next() : "";
                    JSONArray jsonArray = new JSONArray(jsonResponse);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Site site = new Site();
                        site.setId(jsonObject.getInt("id"));
                        site.setSiteName(jsonObject.getString("siteName"));
                        site.setAddress(jsonObject.getString("address"));
                        site.setAddress2(jsonObject.optString("address2", null)); // Handle nullable fields
                        site.setCity(jsonObject.getString("city"));
                        site.setProvinceID(jsonObject.getJSONObject("province").getString("provinceID"));
                        site.setCountry(jsonObject.getString("country"));
                        site.setPostalCode(jsonObject.getString("postalCode"));
                        site.setPhone(jsonObject.getString("phone"));
                        site.setDayOfWeek(jsonObject.getString("dayOfWeek"));
                        site.setDistanceFromWH(jsonObject.getInt("distanceFromWH"));
                        site.setNotes(jsonObject.optString("notes", null));
                        site.setActive(jsonObject.getInt("active") == 1);

                        sites.add(site);
                    }
                }
            } else {
                System.err.println("Failed to fetch sites. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sites;
    }
}