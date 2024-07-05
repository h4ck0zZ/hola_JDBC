
import com.sun.net.httpserver.HttpServer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class Main {

    private static Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                queryParams.put(pair.substring(0, idx), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        return queryParams;
    }

    private static Connection connectJDBC(String url, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Integer httpServerPort = 9011;

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
        httpServer.createContext("/aloha", httpHandler -> {
            String ret = "ok";
            String query = httpHandler.getRequestURI().getQuery();
            System.out.println(query);
            Map<String, String> queryParams = parseQuery(query);
            String host = queryParams.get("host");
            String dbName = queryParams.get("dbName");
            String username = queryParams.get("username");
            String password = queryParams.get("password");

            String jdbcUrl = "jdbc:mysql://" + host + "/" + dbName;
            System.out.println(jdbcUrl);

            Connection connection = connectJDBC(jdbcUrl, username, password);
            if (Objects.nonNull(connection)) {
                ret = "connected.";
            } else {
                ret = "failed.";
            }

            httpHandler.sendResponseHeaders(200, ret.getBytes(StandardCharsets.UTF_8).length);
            final OutputStream responseBody = httpHandler.getResponseBody();
            responseBody.write(ret.getBytes());
            responseBody.close();
        });
        System.out.println("PoC of JDBC vuls. \nServer is listening on: " + httpServerPort);
        httpServer.start();
    }
}
