package utils;

import models.Site;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
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

    private static final String BASE_URL = "http://localhost:8080/api/sites";

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

    public static boolean saveOrUpdateSite(Site site) {
        try {
            boolean isUpdating = site.getId() > 0; // If there's an ID, it's an update
            String endpoint = isUpdating ? BASE_URL + "/edit/" + site.getId() : BASE_URL + "/add";
            String requestMethod = isUpdating ? "PUT" : "POST";

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Convert Site object to JSON
            JSONObject siteJson = new JSONObject();
            if (isUpdating) siteJson.put("id", site.getId()); // Include ID only if updating
            siteJson.put("siteName", site.getSiteName());
            siteJson.put("address", site.getAddress());
            siteJson.put("address2", site.getAddress2() != null ? site.getAddress2() : JSONObject.NULL);
            siteJson.put("city", site.getCity());
            siteJson.put("province", new JSONObject().put("provinceID", site.getProvinceID())); // Nested object
            siteJson.put("country", site.getCountry());
            siteJson.put("postalCode", site.getPostalCode());
            siteJson.put("phone", site.getPhone());
            siteJson.put("dayOfWeek", site.getDayOfWeek());
            siteJson.put("distanceFromWH", site.getDistanceFromWH());
            siteJson.put("notes", site.getNotes() != null ? site.getNotes() : JSONObject.NULL);
            siteJson.put("active", site.isActive() ? 1 : 0);

            // Send request body
            connection.getOutputStream().write(siteJson.toString().getBytes(StandardCharsets.UTF_8));

            // Get response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                return true; // Successfully added/updated
            } else {
                System.err.println("Failed to save site. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}