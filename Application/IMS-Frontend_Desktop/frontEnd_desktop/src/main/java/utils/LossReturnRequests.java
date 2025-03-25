package utils;

import models.Employee;
import models.Txn;
import models.TxnItem;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LossReturnRequests {

    public static boolean submitLossReturnTxn(Txn txn, TxnItem txnItem, boolean resellable, Employee employee) {
        try {
            String json = buildJsonPayload(txn, txnItem, resellable, employee);
            URL url = new URL("http://localhost:8080/api/loss-return/create"); // Adjust if needed
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String buildJsonPayload(Txn txn, TxnItem txnItem, boolean resellable, Employee employee) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"siteId\":").append(txn.getSiteFrom().getId()).append(",");
        sb.append("\"employeeId\":").append(employee.getId()).append(",");
        sb.append("\"txnType\":\"").append(txn.getTxnType().getTxnType()).append("\",");
        sb.append("\"notes\":\"").append(escapeJson(txn.getNotes())).append("\",");

        sb.append("\"itemId\":").append(txnItem.getItemID()).append(",");
        sb.append("\"quantity\":").append(txnItem.getQuantity()).append(",");
        sb.append("\"itemNotes\":\"").append(escapeJson(txnItem.getNotes())).append("\",");
        sb.append("\"resellable\":").append(resellable);

        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}