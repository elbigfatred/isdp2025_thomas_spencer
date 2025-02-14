package utils;

import models.Txn;
import models.TxnItem;
import models.TxnStatus;
import models.TxnType;
import models.Employee;
import models.Site;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TxnRequests {
    private static final String BASE_URL = "http://localhost:8080/api/orders"; // Adjust if needed

    /**
     * Fetches all orders (transactions) for a given site ID.
     *
     * @param siteID The ID of the site whose orders should be fetched.
     * @return A list of Txn objects.
     */
    public static List<Txn> fetchOrdersBySite(int siteID) {
        return fetchOrders(BASE_URL + "/site/" + siteID);
    }

    /**
     * Fetches all orders (transactions) in the system.
     * This is useful for warehouse managers and admins.
     *
     * @return A list of all Txn objects.
     */
    public static List<Txn> fetchAllOrders() {
        return fetchOrders(BASE_URL + "/all");  // Assuming we have an API endpoint for this
    }

    /**
     * Fetches orders based on a given URL.
     *
     * @param urlString The API endpoint URL.
     * @return A list of Txn objects.
     */
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
                    txn.setDeliveryID(obj.isNull("deliveryID") ? null : obj.getInt("deliveryID"));

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

    /**
     * Fetches all items for a given transaction ID.
     *
     * @param txnID The ID of the transaction.
     * @return A list of TxnItem objects.
     */
    public static List<TxnItem> fetchTxnItems(int txnID) {
        List<TxnItem> txnItemsList = new ArrayList<>();

        try {
            String urlString = BASE_URL + "/" + txnID + "/items";
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

                    TxnItem txnItem = new TxnItem();

                    // ✅ Extract transaction ID
                    JSONObject idObject = obj.getJSONObject("id");
                    txnItem.setTxnID(idObject.getInt("txnID"));

                    // ✅ Extract item details
                    JSONObject itemObject = obj.getJSONObject("itemID");
                    txnItem.setItemID(itemObject.getInt("id"));
                    txnItem.setItemName(itemObject.optString("name", "Unknown Item"));
                    txnItem.setItemSku(itemObject.optString("sku", "N/A"));

                    // ✅ Extract quantity
                    txnItem.setQuantity(obj.getInt("quantity"));

                    // ✅ Extract notes safely
                    txnItem.setNotes(obj.optString("notes", ""));

                    txnItemsList.add(txnItem);
                }
            } else {
                System.out.println("[ERROR] Failed to fetch transaction items. Response Code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return txnItemsList;
    }

    /**
     * Submits a given transaction (Marks it as SUBMITTED).
     *
     * @param txnID The transaction ID.
     * @return True if successful, false otherwise.
     */
    public static boolean submitOrder(int txnID) {
        try {
            String urlString = BASE_URL + "/" + txnID + "/submit";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

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

    public static boolean createOrder(Txn order, List<TxnItem> items) {
        try {
            String urlString = BASE_URL + "/"; // Endpoint for creating a new order
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Create JSON payload for order
            JSONObject orderJson = new JSONObject();
            orderJson.put("siteIDTo", order.getSiteTo().getId());
            orderJson.put("siteIDFrom", order.getSiteFrom().getId());
            orderJson.put("txnType", order.getTxnType());
            orderJson.put("employeeID", order.getEmployee().getId());
            orderJson.put("notes", order.getNotes());

            // ✅ Create JSON array for items
            JSONArray itemsArray = new JSONArray();
            for (TxnItem item : items) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("itemID", item.getItemID());
                itemJson.put("quantity", item.getQuantity());
                itemsArray.put(itemJson);
            }
            orderJson.put("items", itemsArray);

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = orderJson.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_CREATED; // 201 Created

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOrderItems(int txnId, List<TxnItem> fulfilledItems, String empUsername) {
        try {
            String urlString = BASE_URL + "/" + txnId + "/update-items" + "?empUsername=" + empUsername;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Create JSON payload
            JSONObject updateJson = new JSONObject();
            updateJson.put("txnID", txnId);

            JSONArray itemsArray = new JSONArray();
            for (TxnItem item : fulfilledItems) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("itemID", item.getItemID());
                itemJson.put("quantity", item.getQuantity());
                itemsArray.put(itemJson);
            }
            updateJson.put("items", itemsArray);

            System.out.println("[DEBUG] JSON Payload for updateOrderItems: " + updateJson.toString(4)); // Print JSON before sending

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = updateJson.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] Response Code from updateOrderItems: " + responseCode);

            return responseCode == HttpURLConnection.HTTP_OK; // 200 OK

        } catch (Exception e) {
            System.out.println("[ERROR] Exception in updateOrderItems:");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateOrderStatus(int txnId, String newStatus, String empUsername) {
        try {
            String urlString = BASE_URL + "/" + txnId + "/update-status?status=" + newStatus + "&empUsername=" + empUsername;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK; // 200 OK

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createBackorder(int siteID, List<TxnItem> backorderItems) {
        try {
            String urlString = BASE_URL + "/backorder";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Build JSON request payload
            JSONObject requestBody = new JSONObject();
            requestBody.put("siteID", siteID);

            JSONArray itemsArray = new JSONArray();
            for (TxnItem item : backorderItems) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("itemID", item.getItemID());
                itemObj.put("quantity", item.getQuantity());
                itemsArray.put(itemObj);
            }
            requestBody.put("items", itemsArray);

            // ✅ Send JSON payload
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes());
            os.flush();
            os.close();

            // ✅ Handle response
            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] Response Code from createBackorder: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("[DEBUG] createBackorder Success!");
                return true;
            } else {
                System.out.println("[ERROR] createBackorder Failed: " + readResponse(conn.getErrorStream()));
                return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception in createBackorder: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}