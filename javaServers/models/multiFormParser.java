package javaServers.models;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class multiFormParser {

    private final Map<String, String> fields = new HashMap<>();
    private String fileName = null;
    private byte[] fileData = null;

    public multiFormParser (HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readAllBytes();

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("boundary=")) {
            throw new IOException("Invalid Content-Type, boundary missing");
        }

        String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);

        if (boundary.contains(";"))
            boundary = boundary.substring(0, boundary.indexOf(';'));

        boundary = ("--" + boundary).trim();
        byte[] boundaryBytes = boundary.getBytes(StandardCharsets.ISO_8859_1);

        // Split body into parts (raw binary-safe split)
        List<byte[]> parts = splitBytes(body, boundaryBytes);

        // Parse each part
        for (byte[] part : parts) {
            if (part.length == 0) continue;
            parsePart(part);
        }
    }

    // ---------------- BINARY SPLIT -----------------

    private List<byte[]> splitBytes(byte[] source, byte[] delimiter) {
        List<byte[]> parts = new ArrayList<>();

        int start = 0;
        for (int i = 0; i < source.length - delimiter.length + 1; i++) {
            boolean match = true;

            for (int j = 0; j < delimiter.length; j++) {
                if (source[i + j] != delimiter[j]) {
                    match = false;
                    break;
                }
            }

            if (match) {
                parts.add(Arrays.copyOfRange(source, start, i));
                start = i + delimiter.length;
            }
        }

        parts.add(Arrays.copyOfRange(source, start, source.length));
        return parts;
    }

    // -------------- PART PARSER -------------------

    private void parsePart(byte[] part) {
        String asText = new String(part, StandardCharsets.ISO_8859_1);
        int headerEnd = asText.indexOf("\r\n\r\n");

        if (headerEnd == -1) return;

        String header = asText.substring(0, headerEnd);
        byte[] content = Arrays.copyOfRange(part, headerEnd + 4, part.length);

        // FILE FIELD
        if (header.contains("filename=\"")) {
            fileName = extractBetween(header, "filename=\"", "\"");
            if (fileName == null || fileName.isEmpty()) return;

            // Remove trailing CRLF
            if (content.length >= 2 &&
                    content[content.length - 2] == '\r' &&
                    content[content.length - 1] == '\n') {
                content = Arrays.copyOfRange(content, 0, content.length - 2);
            }

            fileData = content;
        }
        else {
            // TEXT FIELD
            String fieldName = extractBetween(header, "name=\"", "\"");
            String value = new String(content, StandardCharsets.UTF_8).trim();

            if (fieldName != null)
                fields.put(fieldName, value);
        }
    }

    private String extractBetween(String text, String start, String end) {
        int s = text.indexOf(start);
        if (s == -1) return null;
        s += start.length();
        int e = text.indexOf(end, s);
        if (e == -1) return null;
        return text.substring(s, e);
    }

    // -------------- PUBLIC GETTERS --------------

    public String getField(String key) { return fields.get(key); }

    public boolean hasFile() { return fileName != null && fileData != null; }

    public String getFileName() { return fileName; }

    public byte[] getFileData() { return fileData; }
}