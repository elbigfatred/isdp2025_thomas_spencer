package utils;

import models.Province;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * ProvinceRequests handles API interactions for fetching active provinces.
 */
public class ProvinceRequests {

    private static final String API_URL = "http://localhost:8080/api/provinces/active";

    /**
     * Fetches a list of all active provinces from the backend API.
     *
     * @return A list of Province objects, or an empty list if an error occurs.
     */
    public static List<Province> fetchActiveProvinces() {
        List<Province> provinces = new ArrayList<>();

        try {
            URL url = new URL(API_URL);
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
                        Province province = new Province();
                        province.setProvinceId(jsonObject.getString("provinceID"));
                        province.setProvinceName(jsonObject.getString("provinceName"));
                        province.setCountryCode(jsonObject.getString("countryCode"));
                        province.setActive(jsonObject.getInt("active") == 1);
                        provinces.add(province);
                    }
                }
            } else {
                System.err.println("Failed to fetch provinces. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return provinces;
    }
}