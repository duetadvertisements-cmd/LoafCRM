package javaServers.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ParseFormData {
    public static Map<String, String> parseFormData(InputStream body) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(body));
        StringBuilder requestBody = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        Map<String, String> formData = new HashMap<>();

        for (String pair : requestBody.toString().split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            formData.put(key, value);
        }

        return formData;
    }
}
