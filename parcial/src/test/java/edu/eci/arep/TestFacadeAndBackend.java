
package edu.eci.arep;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class TestFacadeAndBackend {

    public static void main(String[] args) throws Exception {
        testSetKV();
        testGetKVExistingKey();
        testGetKVNonExistingKey();
    }

    private static void testSetKV() throws Exception {
        System.out.println("Test: Crear o actualizar una tupla llave-valor");
        String response = sendRequest("GET /setkv?key=nombre&value=Juan HTTP/1.1\r\n\r\n", "localhost", 36000);
        System.out.println("Response:\n" + response);
    }

    private static void testGetKVExistingKey() throws Exception {
        System.out.println("Test: Obtener el valor de una llave existente");
        String response = sendRequest("GET /getkv?key=nombre HTTP/1.1\r\n\r\n", "localhost", 36000);
        System.out.println("Response:\n" + response);
    }

    private static void testGetKVNonExistingKey() throws Exception {
        System.out.println("Test: Obtener el valor de una llave inexistente");
        String response = sendRequest("GET /getkv?key=apellido HTTP/1.1\r\n\r\n", "localhost", 36000);
        System.out.println("Response:\n" + response);
    }

    private static String sendRequest(String request, String host, int port) throws Exception {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(request);
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }
}