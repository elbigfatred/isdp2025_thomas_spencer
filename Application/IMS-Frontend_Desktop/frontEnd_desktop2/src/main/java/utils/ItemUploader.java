package utils;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;

public class ItemUploader {
    public static boolean updateItem(int itemId, String notes, String imagePath) {
        String endpoint = "http://localhost:8080/api/items/update"; // Adjust as needed

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(endpoint);

            // Build the multipart entity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("id", String.valueOf(itemId)); // Add item ID as text
            builder.addTextBody("notes", notes);              // Add notes as text
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    builder.addBinaryBody("file", imageFile); // Add image file
                } else {
                    System.err.println("Image file does not exist: " + imagePath);
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
}