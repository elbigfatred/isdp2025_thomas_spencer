package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // Generic fetch method for Supplier Orders
    private static List<Txn> fetchOrders(String urlString) {
        List<Txn> txnList = new ArrayList<>();

        try {
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

                    Txn txn = new Txn();
                    txn.setId(obj.getInt("id"));

                    // Employee who created the order
                    if (obj.has("employeeID")) {
                        JSONObject empJson = obj.getJSONObject("employeeID");
                        Employee employee = new Employee();
                        employee.setId(empJson.getInt("id"));
                        employee.setUsername(empJson.optString("username",""));
                        employee.setFirstName(empJson.optString("firstname", ""));
                        employee.setLastName(empJson.optString("lastname", ""));
                        employee.setEmail(empJson.optString("email", ""));
                        txn.setEmployee(employee);
                    }

                    // Receiving and Sending Sites
                    if (obj.has("siteIDTo")) {
                        JSONObject siteToJson = obj.getJSONObject("siteIDTo");
                        Site siteTo = new Site();
                        siteTo.setId(siteToJson.getInt("id"));
                        siteTo.setSiteName(siteToJson.optString("siteName", "Unknown"));
                        txn.setSiteTo(siteTo);
                    }

                    if (obj.has("siteIDFrom")) {
                        JSONObject siteFromJson = obj.getJSONObject("siteIDFrom");
                        Site siteFrom = new Site();
                        siteFrom.setId(siteFromJson.getInt("id"));
                        siteFrom.setSiteName(siteFromJson.optString("siteName", "Unknown"));
                        txn.setSiteFrom(siteFrom);
                    }

                    // Order Type
                    if (obj.has("txnType")) {
                        JSONObject txnTypeJson = obj.getJSONObject("txnType");
                        TxnType txnType = new TxnType();
                        txnType.setTxnType(txnTypeJson.optString("txnType", ""));
                        txn.setTxnType(txnType);
                    }

                    // Order Status
                    if (obj.has("txnStatus")) {
                        JSONObject txnStatusJson = obj.getJSONObject("txnStatus");
                        TxnStatus txnStatus = new TxnStatus();
                        txnStatus.setStatusName(txnStatusJson.optString("statusName", ""));
                        txnStatus.setStatusDescription(txnStatusJson.optString("statusDescription", ""));
                        txn.setTxnStatus(txnStatus);
                    }

                    txn.setBarCode(obj.optString("barCode", "N/A"));
                    txn.setNotes(obj.optString("notes", ""));
                    txn.setEmergencyDelivery(obj.optInt("emergencyDelivery", 0) == 1);
                    txn.setDeliveryID(obj.isNull("deliveryID") ? null : obj.getJSONObject("deliveryID").getInt("id"));
                    // Parse Dates
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    txn.setCreatedDate(LocalDateTime.parse(obj.getString("createdDate"), formatter));
                    if (!obj.isNull("shipDate")) {
                        txn.setShipDate(LocalDateTime.parse(obj.getString("shipDate"), formatter));
                    }

                    txnList.add(txn);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txnList;
    }

    // Fetch method for Supplier Orders
    public static List<Txn> fetchSupplierOrders() {
        String urlString = BASE_URL + "/getAll";  // Adjust if necessary
        return fetchOrders(urlString);
    }

    // Helper method to read response from InputStream
    private static String readResponse(java.io.InputStream inputStream) {
        try (java.util.Scanner scanner = new java.util.Scanner(inputStream)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}