package utils;

import models.Inventory;
import models.Item;
import models.Supplier;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InventoryRequests {

    private static final String BASE_URL = "http://localhost:8080/api/inventory"; // Adjust if needed

    /**
     * Fetches all inventory items for a given site ID.
     *
     * @param siteID The ID of the site whose inventory should be fetched.
     * @return A list of Inventory objects.
     */
    public static List<Inventory> fetchInventoryBySite(int siteID) {
        List<Inventory> inventoryList = new ArrayList<>();

        try {
            String urlString = BASE_URL + "/site/" + siteID;
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

                    Inventory inventory = new Inventory();
                    inventory.setItemID(obj.getJSONObject("id").getInt("itemID"));
                    inventory.setSiteID(obj.getJSONObject("id").getInt("siteID"));
                    inventory.setItemLocation(obj.getJSONObject("id").getString("itemLocation"));
                    inventory.setQuantity(obj.getInt("quantity"));
                    inventory.setReorderThreshold(obj.optInt("reorderThreshold", 0));
                    inventory.setOptimumThreshold(obj.optInt("optimumThreshold", 0));
                    inventory.setNotes(obj.optString("notes", ""));

                    // ✅ Extract only relevant Item fields
                    if (obj.has("item")) {
                        JSONObject itemJson = obj.getJSONObject("item");
                        Item item = new Item();

                        item.setId(itemJson.getInt("id"));
                        item.setName(itemJson.optString("name", "Unknown Item"));
                        item.setSku(itemJson.optString("sku", ""));
                        item.setActive(itemJson.optInt("active", 1) == 1);
                        item.setCaseSize(itemJson.optInt("caseSize", 1));

                        if (itemJson.has("supplier")){
                            JSONObject supplierJson = itemJson.getJSONObject("supplier");
                            Supplier supplier = new Supplier();

                            supplier.setId(supplierJson.getInt("supplierid"));
                            supplier.setName(supplierJson.getString("name"));
                            supplier.setActive(supplierJson.getInt("active") == 1);

                            item.setSupplier(supplier);
                        }

                        inventory.setItem(item);
                    }

                    inventoryList.add(inventory);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventoryList;
    }    /**
     * Updates an existing inventory record.
     *
     * @param inventory The inventory object to update.
     * @return True if successful, false otherwise.
     */
    public static boolean updateInventory(Inventory inventory) {
        try {

            System.out.println("[DEBUG] itemLocation being sent: " + inventory.getItemLocation());
            // ✅ Construct the URL with path variables
            String urlString = String.format("%s/edit/%d/%d/%s",
                    BASE_URL, inventory.getItemID(), inventory.getSiteID(), inventory.getItemLocation());

            System.out.println("[DEBUG] Sending PUT request to: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Create JSON payload with only required fields
            JSONObject payload = new JSONObject();
            payload.put("reorderThreshold", inventory.getReorderThreshold());
            payload.put("optimumThreshold", inventory.getOptimumThreshold());
            payload.put("notes", inventory.getNotes());

            System.out.println("[DEBUG] JSON Payload to send: " + payload.toString(4)); // Pretty print JSON

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] Response Code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("[ERROR] Server responded with: " + responseCode);
            } else {
                System.out.println("[SUCCESS] Inventory updated successfully.");
            }

            conn.disconnect();
            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            System.out.println("[ERROR] Exception occurred while updating inventory:");
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
     * Sends a POST request to increment inventory at a given site.
     *
     * @param siteID The site where inventory should be increased.
     * @param items  The list of inventory items to increment.
     * @return True if successful, false otherwise.
     */
    public static boolean incrementInventory(int siteID, List<Inventory> items) {
        return sendInventoryUpdateRequest(siteID, items, "/increment");
    }

    /**
     * Sends a POST request to decrement inventory at a given site.
     *
     * @param siteID The site where inventory should be decreased.
     * @param items  The list of inventory items to decrement.
     * @return True if successful, false otherwise.
     */
    public static boolean decrementInventory(int siteID, List<Inventory> items) {
        return sendInventoryUpdateRequest(siteID, items, "/decrement");
    }

    /**
     * Helper method to send an inventory update request.
     *
     * @param siteID The site ID.
     * @param items  The inventory items.
     * @param endpoint The API endpoint ("/increment" or "/decrement").
     * @return True if successful, false otherwise.
     */
    private static boolean sendInventoryUpdateRequest(int siteID, List<Inventory> items, String endpoint) {
        try {
            String urlString = BASE_URL + endpoint;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Build JSON request payload
            JSONObject requestBody = new JSONObject();
            requestBody.put("siteID", siteID);

            JSONArray itemsArray = new JSONArray();
            for (Inventory item : items) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("itemID", item.getItemID());
                itemObj.put("quantity", item.getQuantity());
                itemsArray.put(itemObj);
            }
            requestBody.put("items", itemsArray);

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("utf-8"));
                os.flush();
            }

            // ✅ Handle response
            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] Response Code from " + endpoint + ": " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("[SUCCESS] Inventory update successful.");
                return true;
            } else {
                System.out.println("[ERROR] Inventory update failed: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception in sendInventoryUpdateRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<Inventory> getViableSupplierInventory(){
        List<Inventory> inventoryList = fetchInventoryBySite(2);
        List<Inventory> finalList = new ArrayList<>();
        for (Inventory inventory : inventoryList) {
            Item item = inventory.getItem();
            if (item.getSupplier().getActive()){
                finalList.add(inventory);
            }
        }
        return finalList;
    }

}