package edu.eci.arep.facade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FacadeServer {
    private static final int PORT = 36000;
    private static final String BACKEND_HOST = "localhost";
    private static final int BACKEND_PORT = 35000;
    private static final int MAX_LENGTH = 50;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Facade server ready on port: " + PORT);

        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                handleClient(clientSocket);
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
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
    }

    private static String processRequest(String requestLine) {
        try {
            if (requestLine.startsWith("GET /setkv")) {
                return handleSetKV(requestLine);
            } else if (requestLine.startsWith("GET /getkv")) {
                return handleGetKV(requestLine);
            } else if (requestLine.startsWith("GET /client")) {
                return serveClientHtml();
            } else {
                return buildHttpResponse(400, "Invalid request");
            }
        } catch (Exception e) {
            return buildHttpResponse(500, "Internal server error");
        }
    }

    private static String handleSetKV(String requestLine) throws IOException {
        HashMap<String, String> params = parseParams(requestLine);
        String key = params.get("key");
        String value = params.get("value");

        if (key == null || value == null) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key or value\", \"message\": \"Key or value is missing\"}");
        }

        key = key.trim();
        value = value.trim();

        if (key.isEmpty() || value.isEmpty()) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key or value\", \"message\": \"Key or value cannot be empty\"}");
        }

        if (key.length() > MAX_LENGTH || value.length() > MAX_LENGTH) {
            return buildHttpResponse(400, String.format(
                "{\"error\": \"Invalid key or value\", \"message\": \"Key or value exceeds maximum length of %d characters\"}", MAX_LENGTH));
        }

        return forwardToBackend("GET /setkv?key=" + key + "&value=" + value);
    }

    private static String handleGetKV(String requestLine) throws IOException {
        HashMap<String, String> params = parseParams(requestLine);
        String key = params.get("key");

        if (key == null) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key\", \"message\": \"Key is missing\"}");
        }

        key = key.trim();

        if (key.isEmpty()) {
            return buildHttpResponse(400, "{\"error\": \"Invalid key\", \"message\": \"Key cannot be empty\"}");
        }

        if (key.length() > MAX_LENGTH) {
            return buildHttpResponse(400, String.format(
                "{\"error\": \"Invalid key\", \"message\": \"Key exceeds maximum length of %d characters\"}", MAX_LENGTH));
        }

        return forwardToBackend("GET /getkv?key=" + key);
    }

    private static String forwardToBackend(String requestLine) throws IOException {
        try (Socket backendSocket = new Socket(BACKEND_HOST, BACKEND_PORT);
             PrintWriter out = new PrintWriter(backendSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(backendSocket.getInputStream()))) {

            out.println(requestLine);
            StringBuilder response = new StringBuilder();
            String line;
            int statusCode = 200; 
            while ((line = in.readLine()) != null) {
                if (line.startsWith("HTTP/1.1")) {
                    statusCode = Integer.parseInt(line.split(" ")[1]);
                }
                response.append(line).append("\n");
                if (!in.ready()) {
                    break;
                }
            }

            
            return buildHttpResponse(statusCode, response.toString().split("\r\n\r\n", 2)[1]);
        }
    }

    private static String serveClientHtml() {
        String html = """
            HTTP/1.1 200 OK\r
            Content-Type: text/html; charset=utf-8\r
            \r
            <!DOCTYPE html>
            <html>
            <head>
                <title>Key-Value Store</title>
            </head>
            <body>
                <h1>Key-Value Store</h1>
                <form>
                    <label for="key">Key:</label>
                    <input type="text" id="key" name="key"><br>
                    <label for="value">Value:</label>
                    <input type="text" id="value" name="value"><br>
                    <button type="button" onclick="setKV()">Set</button>
                    <button type="button" onclick="getKV()">Get</button>
                </form>
                <div id="response"></div>
                <script>
                    function setKV() {
                        const key = document.getElementById("key").value;
                        const value = document.getElementById("value").value;
                        fetch(`/setkv?key=${key}&value=${value}`)
                            .then(response => response.json())
                            .then(data => document.getElementById("response").innerText = JSON.stringify(data));
                    }
                    function getKV() {
                        const key = document.getElementById("key").value;
                        fetch(`/getkv?key=${key}`)
                            .then(response => response.json())
                            .then(data => document.getElementById("response").innerText = JSON.stringify(data));
                    }
                </script>
            </body>
            </html>
        """;
        return html;
    }

    private static HashMap<String, String> parseParams(String requestLine) {
        HashMap<String, String> params = new HashMap<>();
        String[] parts = requestLine.split(" ")[1].split("\\?");
        if (parts.length > 1) {
            String[] keyValuePairs = parts[1].split("&");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1].trim());
                }
            }
        }
        return params;
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