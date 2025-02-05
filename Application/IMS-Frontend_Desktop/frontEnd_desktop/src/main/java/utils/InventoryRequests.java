package utils;

import models.Inventory;
import models.Item;
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
            URL url = new URL(BASE_URL + "/update");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Create JSON payload with only required fields
            JSONObject payload = new JSONObject();
            JSONObject idObject = new JSONObject();

            idObject.put("itemID", inventory.getItemID());
            idObject.put("siteID", inventory.getSiteID());

            payload.put("id", idObject);
            payload.put("reorderThreshold", inventory.getReorderThreshold());
            payload.put("optimumThreshold", inventory.getOptimumThreshold());
            payload.put("notes", inventory.getNotes());

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
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
}