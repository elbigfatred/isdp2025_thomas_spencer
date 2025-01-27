package utils;

import models.Category;
import models.Item;
import models.Province;
import models.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ItemRequests {

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
                        supplier.setId(supplierJson.getInt("id"));
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

    // New method to deactivate an item
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

}
