package edu.eci.arep.backend;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class BackendServer {
    private static final int PORT = 35000;
    private static final HashMap<String, String> keyValueStore = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Listo para recibir: " + PORT);
        } catch (IOException e) {
            System.err.println("Accept failed. " + PORT);
            System.exit(1);
        }

        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                System.err.println("Accept failed.");
            }
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        String inputLine;
        StringBuilder request = new StringBuilder();


        while ((inputLine = in.readLine()) != null) {
            request.append(inputLine).append("\n");
            if (!in.ready()) {
                break;
            }
        }
        String requestLine = request.toString().split("\n")[0];
        String response = processRequest(requestLine);
        out.println(response);
        out.close();
        in.close();
        clientSocket.close();
    }

    private static String processRequest(String requestLine) {
        try {
            if (requestLine.startsWith("GET /setkv")) {
                return handleSetKV(requestLine);
            } else if (requestLine.startsWith("GET /getkv")) {
                return handleGetKV(requestLine);
            } else {
                return buildHttpResponse(400, "Invalid request");
            }
        } catch (Exception e) {
            return buildHttpResponse(500, "Internal server error");
        }
    }

    private static String handleSetKV(String requestLine) {
        String[] parts = requestLine.split(" ")[1].split("\\?");
        if (parts.length < 2) {
            return buildHttpResponse(400, "Missing parameters");
        }

        String[] params = parts[1].split("&");
        String key = null, value = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("key")) {
                key = keyValue.length > 1 ? keyValue[1].trim() : null;
            } else if (keyValue[0].equals("value")) {
                value = keyValue.length > 1 ? keyValue[1].trim() : null;
            }
        }

        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key or value\"}");
        }

        boolean isUpdate = keyValueStore.containsKey(key);
        keyValueStore.put(key, value);

        String status = isUpdate ? "updated" : "created";
        String jsonResponse = String.format("{\"key\": \"%s\", \"value\": \"%s\", \"status\": \"%s\"}", key, value, status);
        return buildHttpResponse(200, jsonResponse);
    }

    private static String handleGetKV(String requestLine) {
        String[] parts = requestLine.split(" ")[1].split("\\?");
        if (parts.length < 2) {
            return buildHttpResponse(400, "Missing parameters");
        }

        String[] params = parts[1].split("&");
        String key = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("key")) {
                key = keyValue.length > 1 ? keyValue[1].trim() : null;
            }
        }

        if (key == null || key.isEmpty()) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key\"}");
        }

        System.out.println("Received GET /getkv request for key: " + key);
        System.out.println("KeyValueStore contents: " + keyValueStore);

        if (keyValueStore.containsKey(key)) {
            String value = keyValueStore.get(key);
            String jsonResponse = String.format("{\"key\": \"%s\", \"value\": \"%s\"}", key, value);
            return buildHttpResponse(200, jsonResponse);
        } else {
            String jsonResponse = String.format("{\"error\": \"key_not_found\", \"key\": \"%s\"}", key);
            return buildHttpResponse(404, jsonResponse);
        }
    }

    private static String buildHttpResponse(int statusCode, String body) {
        String statusMessage = switch (statusCode) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };

        return String.format(
            "HTTP/1.1 %d %s\r\n" +
            "Content-Type: application/json; charset=utf-8\r\n" +
            "\r\n" +
            "%s",
            statusCode, statusMessage, body
        );
    }
}