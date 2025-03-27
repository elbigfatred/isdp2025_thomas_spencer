package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import models.Inventory;
import models.Txn;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.TxnItem;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SupplierOrderRequests {
    private static final String BASE_URL = "http://localhost:8080/api/supplierorders";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Integer getActiveOrderId() {
        try {
            URL url = new URL(BASE_URL + "/check-active");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = in.readLine();
                    if (response != null && !response.equals("null")) {
                        return Integer.parseInt(response);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // no active order
    }

    public static boolean createNewSupplierOrder(int employeeId) {
        try {
            URL url = new URL(BASE_URL + "/createNew");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Build minimal JSON payload
            String json = "{\"employeeID\":" + employeeId + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            return conn.getResponseCode() == 201;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateItems(List<Inventory> inventoryItems, int txnId, String empUsername) {
        try {
            URL url = new URL(BASE_URL + "/" + txnId + "/update-items?empUsername=" + empUsername);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            // Manually construct the JSON payload
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"txnID\":").append(txnId).append(",");
            jsonBuilder.append("\"items\":[");

            for (int i = 0; i < inventoryItems.size(); i++) {
                Inventory inv = inventoryItems.get(i);
                jsonBuilder.append("{")
                        .append("\"itemID\":").append(inv.getItemID()).append(",")
                        .append("\"quantity\":").append(inv.getQuantity())
                        .append("}");
                if (i < inventoryItems.size() - 1) {
                    jsonBuilder.append(",");
                }
            }

            jsonBuilder.append("]}");
            String jsonPayload = jsonBuilder.toString();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
            }

            return conn.getResponseCode() == 200;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean submitSupplierOrder(int txnId, String empUsername) {
        try {
            URL url = new URL(BASE_URL + "/" + txnId + "/submit?empUsername=" + empUsername);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<TxnItem> fetchTxnItems(int txnId) {
        List<TxnItem> items = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/api/orders/" + txnId + "/items");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    for (JsonNode node : root) {
                        int itemId;

                        // Some responses may use "itemID", others may use "id"
                        if (node.has("itemID")) {
                            itemId = node.get("itemID").get("id").asInt();
                        } else {
                            continue; // Skip if no identifiable ID
                        }

                        System.out.println(itemId);


                        int quantity = node.has("quantity") ? node.get("quantity").asInt() : 0;

                        TxnItem item = new TxnItem();
                        item.setItemID(itemId);
                        item.setQuantity(quantity);
                        items.add(item);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }
}