package utils;

import models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TxnsModsRequests {
    private static final String BASE_URL = "http://localhost:8080/api/txns";

    /**
     * Fetches all transactions from the backend API.
     *
     * @return List of transactions or an empty list if an error occurs.
     */
    public static List<Txn> fetchAllTransactions() {
        List<Txn> transactions = new ArrayList<>();

        try {
            URL url = new URL(BASE_URL);
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
            txn.setEmergencyDelivery(jsonTxn.optBoolean("emergencyDelivery", false));
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
}
