package utils;

import models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TxnsModsRequests {
    private static final String BASE_URLTXNS = "http://localhost:8080/api/txns";
    private static final String BASE_URL = "http://localhost:8080/api/";

    /**
     * Fetches all transactions from the backend API.
     *
     * @return List of transactions or an empty list if an error occurs.
     */
    public static List<Txn> fetchAllTransactions() {
        List<Txn> transactions = new ArrayList<>();

        try {
            URL url = new URL(BASE_URLTXNS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("[ERROR] Failed to fetch transactions. HTTP Code: " + responseCode);
                return transactions;
            }

            // ✅ Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // ✅ Parse JSON response
            JSONArray jsonArray = new JSONArray(response.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTxn = jsonArray.getJSONObject(i);
                Txn txn = parseTxnFromJSON(jsonTxn);
                transactions.add(txn);
            }

            System.out.println("[SUCCESS] Fetched " + transactions.size() + " transactions.");
        } catch (Exception e) {
            System.out.println("[ERROR] Exception in fetchAllTransactions: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    /**
     * Parses a JSON object into a Txn object.
     *
     * @param jsonTxn JSON representation of a transaction.
     * @return Txn object.
     */
    private static Txn parseTxnFromJSON(JSONObject jsonTxn) {
        Txn txn = new Txn();
        try {
            txn.setId(jsonTxn.getInt("id"));
            txn.setBarCode(jsonTxn.optString("barCode", ""));
            //txn.setEmergencyDelivery(jsonTxn.optBoolean("emergencyDelivery", false));
            txn.setEmergencyDelivery(jsonTxn.optInt("emergencyDelivery", 0) == 1);
            txn.setCreatedDate(parseDateTime(jsonTxn.optString("createdDate", null)));
            txn.setShipDate(parseDateTime(jsonTxn.optString("shipDate", null)));

            // ✅ Set Transaction Status
            if (jsonTxn.has("txnStatus")) {
                TxnStatus status = new TxnStatus();
                status.setStatusName(jsonTxn.getJSONObject("txnStatus").getString("statusName"));
                txn.setTxnStatus(status);
            }

            // ✅ Set Transaction Type
            if (jsonTxn.has("txnType")) {
                TxnType type = new TxnType();
                type.setTxnType(jsonTxn.getJSONObject("txnType").getString("txnType"));
                txn.setTxnType(type);
            }

            // ✅ Set Destination Site
            if (jsonTxn.has("siteIDTo")) {
                Site site = new Site();
                site.setId(jsonTxn.getJSONObject("siteIDTo").getInt("id"));
                site.setSiteName(jsonTxn.getJSONObject("siteIDTo").getString("siteName"));
                txn.setSiteTo(site);
            }

            // ✅ Set Source Site
            if (jsonTxn.has("siteIDFrom")) {
                Site site = new Site();
                site.setId(jsonTxn.getJSONObject("siteIDFrom").getInt("id"));
                site.setSiteName(jsonTxn.getJSONObject("siteIDFrom").getString("siteName"));
                txn.setSiteFrom(site);
            }

            // ✅ Set Employee
            if (jsonTxn.has("employee")) {
                Employee emp = new Employee();
                emp.setUsername(jsonTxn.getJSONObject("employee").getString("username"));
                txn.setEmployee(emp);
            }


            // ✅ Set Delivery ID (Handle Nested Object)
            if (jsonTxn.has("deliveryID") && !jsonTxn.isNull("deliveryID")) {
                JSONObject jsonDelivery = jsonTxn.getJSONObject("deliveryID");
                txn.setDeliveryID(jsonDelivery.getInt("id")); // Extract ID from nested object
            } else {
                txn.setDeliveryID(null);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to parse Txn JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return txn;
    }

    /**
     * Parses a date-time string into LocalDateTime.
     *
     * @param dateTimeStr The date-time string.
     * @return LocalDateTime or null if parsing fails.
     */
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || "null".equalsIgnoreCase(dateTimeStr)) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to parse date: " + dateTimeStr);
            return null;
        }
    }

    // ✅ Fetch all Sites
    public static List<Site> getAllSites() {
        List<Site> sites = new ArrayList<>();
        try {
            JSONArray jsonArray = fetchJSONArray("sites");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSite = jsonArray.getJSONObject(i);
                Site site = new Site();
                site.setId(jsonSite.getInt("id"));
                site.setSiteName(jsonSite.getString("siteName"));
                sites.add(site);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch sites: " + e.getMessage());
            e.printStackTrace();
        }
        return sites;
    }

    // ✅ Fetch all Transaction Statuses
    public static List<TxnStatus> getAllTxnStatuses() {
        List<TxnStatus> statuses = new ArrayList<>();
        try {
            JSONArray jsonArray = fetchJSONArray("txns/txnStatuses");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonStatus = jsonArray.getJSONObject(i);
                TxnStatus status = new TxnStatus();
                status.setStatusName(jsonStatus.getString("statusName"));
                statuses.add(status);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch transaction statuses: " + e.getMessage());
            e.printStackTrace();
        }
        return statuses;
    }

    // ✅ Fetch all Transaction Types
    public static List<TxnType> getAllTxnTypes() {
        List<TxnType> txnTypes = new ArrayList<>();
        try {
            JSONArray jsonArray = fetchJSONArray("txns/txnTypes");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonType = jsonArray.getJSONObject(i);
                TxnType txnType = new TxnType();
                txnType.setTxnType(jsonType.getString("txnType"));
                txnTypes.add(txnType);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch transaction types: " + e.getMessage());
            e.printStackTrace();
        }
        return txnTypes;
    }

    // ✅ Fetch all Delivery IDs
    public static List<Integer> getAllDeliveryIds() {
        List<Integer> deliveryIds = new ArrayList<>();
        try {
            JSONArray jsonArray = fetchJSONArray("delivery/all");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonDelivery = jsonArray.getJSONObject(i);
                deliveryIds.add(jsonDelivery.getInt("id"));
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch delivery IDs: " + e.getMessage());
            e.printStackTrace();
        }
        return deliveryIds;
    }

    // ✅ Utility method to fetch JSON from API
    private static JSONArray fetchJSONArray(String endpoint) {
        try {
            URL url = new URL(BASE_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("[ERROR] HTTP GET Request Failed: " + conn.getResponseCode());
            }

            Scanner scanner = new Scanner(url.openStream());
            StringBuilder jsonStr = new StringBuilder();
            while (scanner.hasNext()) {
                jsonStr.append(scanner.nextLine());
            }
            scanner.close();
            return new JSONArray(jsonStr.toString());

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch data from API: " + e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public static boolean updateTxn(int txnId, int siteIDTo, String txnStatus, String shipDate,
                                    String txnType, String barCode, Integer deliveryID, boolean emergencyDelivery) {
        try {
            String urlString = BASE_URL + "txns/" + txnId;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // ✅ Build JSON request payload
            JSONObject requestBody = new JSONObject();
            requestBody.put("siteIDTo", siteIDTo);
            requestBody.put("txnStatus", txnStatus);
            requestBody.put("shipDate", shipDate); // Ensure it's in correct format (ISO 8601)
            requestBody.put("txnType", txnType);
            requestBody.put("barCode", barCode);
            requestBody.put("deliveryID", deliveryID != null ? deliveryID : JSONObject.NULL);
            requestBody.put("emergencyDelivery", emergencyDelivery ? 1 : 0); // Convert boolean to int

            // ✅ Send JSON payload
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
                os.flush();
            }

            // ✅ Handle response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("[SUCCESS] Transaction updated successfully.");
                return true;
            } else {
                System.out.println("[ERROR] Failed to update transaction. Response Code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception in updateTxn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
