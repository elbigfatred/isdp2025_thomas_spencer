package utils;

import models.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SupplierUtil {
    private static final String BASE_URL = "http://localhost:8080/api/suppliers"; // Adjust if needed

    /**
     * Fetches all suppliers from the API.
     *
     * @param includeInactive If true, includes inactive suppliers.
     * @return A list of Supplier objects.
     */
    public static List<Supplier> fetchAllSuppliers(boolean includeInactive) {
        List<Supplier> supplierList = new ArrayList<>();
        try {
            String urlString = BASE_URL + "?includeInactive=" + includeInactive;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonResponse = readResponse(conn.getInputStream());
                JSONArray responseArray = new JSONArray(jsonResponse);

                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject obj = responseArray.getJSONObject(i);
                    supplierList.add(parseSupplier(obj));
                }
            } else {
                System.out.println("[ERROR] Failed to fetch suppliers. Response Code: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return supplierList;
    }

    /**
     * Fetches a supplier by ID.
     *
     * @param supplierId The ID of the supplier.
     * @return A Supplier object or null if not found.
     */
    public static Supplier fetchSupplierById(int supplierId) {
        try {
            String urlString = BASE_URL + "/" + supplierId;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String jsonResponse = readResponse(conn.getInputStream());
                return parseSupplier(new JSONObject(jsonResponse));
            } else {
                System.out.println("[ERROR] Supplier not found. Response Code: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new supplier.
     *
     * @param supplier The Supplier object to add.
     * @return True if successful, false otherwise.
     */
    public static boolean addSupplier(Supplier supplier) {
        try {
            String urlString = BASE_URL;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = buildSupplierJson(supplier);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing supplier.
     *
     * @param supplierId The ID of the supplier to update.
     * @param supplier   The updated Supplier object.
     * @return True if successful, false otherwise.
     */
    public static boolean updateSupplier(int supplierId, Supplier supplier) {
        try {
            String urlString = BASE_URL + "/" + supplierId;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = buildSupplierJson(supplier);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     */
    private static String readResponse(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * Parses a JSON object into a Supplier object.
     */
    private static Supplier parseSupplier(JSONObject obj) {
        Supplier supplier = new Supplier();
        supplier.setId(obj.getInt("supplierid"));
        supplier.setName(obj.getString("name"));
        supplier.setAddress1(obj.getString("address1"));
        supplier.setAddress2(obj.optString("address2", ""));
        supplier.setCity(obj.getString("city"));
        supplier.setCountry(obj.getString("country"));
        supplier.setProvinceId(obj.getJSONObject("province").getString("provinceID"));
        supplier.setPostalcode(obj.getString("postalcode"));
        supplier.setPhone(obj.getString("phone"));
        supplier.setContact(obj.optString("contact", ""));
        supplier.setNotes(obj.optString("notes", ""));
        supplier.setActive(obj.getInt("active") == 1);
        return supplier;
    }

    /**
     * Builds a JSON string from a Supplier object.
     */
    private static String buildSupplierJson(Supplier supplier) {
        JSONObject obj = new JSONObject();
        obj.put("name", supplier.getName());
        obj.put("address1", supplier.getAddress1());
        obj.put("address2", supplier.getAddress2());
        obj.put("city", supplier.getCity());
        obj.put("country", supplier.getCountry());

        JSONObject provinceObj = new JSONObject();
        provinceObj.put("provinceID", supplier.getProvinceId());
        obj.put("province", provinceObj);

        obj.put("postalcode", supplier.getPostalcode());
        obj.put("phone", supplier.getPhone());
        obj.put("contact", supplier.getContact());
        obj.put("notes", supplier.getNotes());
        obj.put("active", supplier.getActive() ? 1 : 0);

        return obj.toString();
    }
}