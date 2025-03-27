package utils;

import models.Category;
import models.Item;
import models.Province;
import models.Supplier;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * ItemRequests handles API interactions for managing inventory items.
 *
 * Features:
 * - Fetch all items from the backend with detailed attributes.
 * - Parse category, supplier, and province information from the response.
 * - Deactivate an item by ID via a PUT request.
 * - Update item details, including notes and image uploads.
 * - Uses `HttpURLConnection` for API communication.
 */
public class ItemRequests {

    /**
     * Fetches a list of all items from the backend API.
     * Parses JSON data and maps it to Item objects with detailed attributes.
     *
     * @return A list of Item objects, or an empty list if an error occurs.
     */
    public static List<Item> fetchItems() {
        String endpoint = "http://localhost:8080/api/items";
        List<Item> items = new ArrayList<>();

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
                        Item item = new Item();
                        item.setId(jsonObject.getInt("id"));
                        item.setName(jsonObject.getString("name"));
                        item.setSku(jsonObject.getString("sku"));
                        item.setDescription(jsonObject.optString("description", null)); // Handle nullable fields
                        item.setWeight(jsonObject.getBigDecimal("weight"));
                        item.setCaseSize(jsonObject.getInt("caseSize"));
                        item.setCostPrice(jsonObject.getBigDecimal("costPrice"));
                        item.setRetailPrice(jsonObject.getBigDecimal("retailPrice"));
                        item.setNotes(jsonObject.optString("notes", null));
                        item.setActive(jsonObject.getInt("active") == 1);
                        item.setImageLocation(jsonObject.optString("imageLocation", null));

                        // Parse category
                        JSONObject categoryJson = jsonObject.getJSONObject("category");
                        Category category = new Category();
                        category.setCategoryName(categoryJson.getString("categoryName"));
                        category.setActive(categoryJson.getInt("active") == 1);
                        item.setCategory(category);

                        // Parse supplier
                        JSONObject supplierJson = jsonObject.getJSONObject("supplier");
                        Supplier supplier = new Supplier();
                        supplier.setId(supplierJson.getInt("supplierid"));
                        supplier.setName(supplierJson.getString("name"));
                        supplier.setAddress1(supplierJson.getString("address1"));
                        supplier.setAddress2(supplierJson.optString("address2", null)); // Handle nullable fields
                        supplier.setCity(supplierJson.getString("city"));
                        supplier.setCountry(supplierJson.getString("country"));

                        // Parse province
                        JSONObject provinceJson = supplierJson.getJSONObject("province");
                        Province province = new Province();
                        province.setProvinceId(provinceJson.getString("provinceID"));
                        supplier.setProvinceId(province.getProvinceId());

                        supplier.setPostalcode(supplierJson.getString("postalcode"));
                        supplier.setPhone(supplierJson.getString("phone"));
                        supplier.setContact(supplierJson.optString("contact", null)); // Handle nullable fields
                        supplier.setNotes(supplierJson.optString("notes", null));
                        supplier.setActive(supplierJson.getInt("active") == 1);
                        item.setSupplier(supplier);

                        items.add(item);
                    }
                }
            } else {
                System.err.println("Failed to fetch items. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Deactivates an item by its ID using a PUT request.
     * Returns true if the item was successfully deactivated.
     *
     * @param itemId The ID of the item to deactivate.
     * @return true if successful, false otherwise.
     */
    public static boolean deactivateItem(int itemId) {
        String endpoint = "http://localhost:8080/api/items/" + itemId + "/deactivate";

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Item deactivated successfully.");
                return true;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                System.err.println("Item not found.");
            } else {
                System.err.println("Failed to deactivate item. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public static boolean updateItem(Item item) {
        String endpoint = "http://localhost:8080/api/items/update"; // Adjust as needed

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endpoint);

            // Build the multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("id", String.valueOf(item.getId()));           // Add item ID as text
            builder.addTextBody("name", item.getName());                       // Add item name
            builder.addTextBody("notes", item.getNotes());                     // Add notes as text
            builder.addTextBody("desc", item.getDescription());                // Add description
            builder.addTextBody("category", item.getCategory().getCategoryName()); // Add category name
            builder.addTextBody("supplier", String.valueOf(item.getSupplier().getId())); // Add supplier id
            builder.addTextBody("caseSize", String.valueOf(item.getCaseSize())); // Add case size
            builder.addTextBody("weight", item.getWeight().toString());        // Add weight
            builder.addTextBody("active", item.getActive() ? "1" : "0");      // Add active status

            // Handle image using the imageLocation field
            String imageLocation = item.getImageLocation();
            if (imageLocation != null && !imageLocation.isEmpty()) {
                File imageFile = new File(imageLocation);
                if (imageFile.exists()) {
                    builder.addBinaryBody("file", imageFile); // Add image file
                } else {
                    System.err.println("Image file does not exist: " + imageLocation);
                    return false;
                }
            }

            // Attach the multipart entity to the request
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                if (statusCode == 200) {
                    System.out.println("Success: " + responseBody);
                    return true;
                } else {
                    System.err.println("Failed to upload. HTTP Code: " + statusCode);
                    System.err.println("Response: " + responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createItem(Item item) {
        String endpoint = "http://localhost:8080/api/items/create"; // Adjusted for the create endpoint

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endpoint);

            // Build the multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.addTextBody("name", item.getName());                       // Add item name
            builder.addTextBody("notes", item.getNotes());                     // Add notes as text
            builder.addTextBody("desc", item.getDescription());                // Add description
            builder.addTextBody("category", item.getCategory().getCategoryName()); // Add category name
            builder.addTextBody("supplier", String.valueOf(item.getSupplier().getId())); // Add supplier id
            builder.addTextBody("caseSize", String.valueOf(item.getCaseSize())); // Add case size
            builder.addTextBody("weight", item.getWeight().toString());        // Add weight
            builder.addTextBody("active", item.getActive() ? "1" : "0");      // Add active status
            builder.addTextBody("costPrice", item.getCostPrice().toString());  // Add cost price
            builder.addTextBody("retailPrice", item.getRetailPrice().toString()); // Add retail price

            // Handle image using the imageLocation field if there's an image to upload
            String imageLocation = item.getImageLocation();
            if (imageLocation != null && !imageLocation.isEmpty()) {
                File imageFile = new File(imageLocation);
                if (imageFile.exists()) {
                    builder.addBinaryBody("file", imageFile); // Add image file
                } else {
                    System.err.println("Image file does not exist: " + imageLocation);
                    return false;
                }
            }

            // Attach the multipart entity to the request
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            // Execute the request and handle the response
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                if (statusCode == 201) {  // 201 for Created
                    System.out.println("Success: " + responseBody);
                    return true;
                } else {
                    System.err.println("Failed to create item. HTTP Code: " + statusCode);
                    System.err.println("Response: " + responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches an item's image from the backend.
     * Checks if the image exists before returning it.
     *
     * @param itemId The ID of the item.
     * @param width  Desired width of the image.
     * @param height Desired height of the image.
     * @return ImageIcon if found, or null if not.
     */
    public static ImageIcon fetchItemImage(int itemId, int width, int height) {
        final String BASE_URL = "http://localhost:8080/api/items/image/";
        try {
            URL imageUrl = new URL(BASE_URL + itemId);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Check if the response is HTTP 200 (OK)
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ImageIcon itemImage = new ImageIcon(imageUrl);
                Image scaledImage = itemImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                connection.disconnect();
                return new ImageIcon(scaledImage);
            } else {
                connection.disconnect();
                return null; // No image found
            }
        } catch (Exception e) {
            return null; // Handle connection errors
        }
    }

    public static List<Category> fetchCategories() {
        String endpoint = "http://localhost:8080/api/category"; // Adjust the URL to match your API endpoint
        List<Category> categories = new ArrayList<>();

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
                        Category category = new Category();
                        category.setCategoryName(jsonObject.getString("categoryName"));
                        category.setActive(jsonObject.getInt("active") == 1); // Assuming active is stored as 1 or 0

                        categories.add(category);
                    }
                }
            } else {
                System.err.println("Failed to fetch categories. HTTP Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }
}
