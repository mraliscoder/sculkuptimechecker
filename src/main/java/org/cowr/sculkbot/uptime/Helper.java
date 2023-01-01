package org.cowr.sculkbot.uptime;

import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    private static final Logger log = LogManager.getLogger(Helper.class);

    public static void noMethodExist(HttpExchange httpExchange) {
        error(httpExchange, 400, "No " + httpExchange.getRequestMethod() + " handler exists for endpoint " + httpExchange.getRequestURI());
    }
    public static void error(HttpExchange httpExchange, int code, String description) {
        try {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", code);
            error.put("errorDescription", description);
            error.put("version", Checker.VERSION);

            String answer = error.toString();
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(200, answer.getBytes(StandardCharsets.UTF_8).length);

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(answer.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

        } catch (IOException ignored) {}
    }

    public static void ok(HttpExchange httpExchange) {
        ok(httpExchange, null);
    }

    public static void ok(HttpExchange httpExchange, JSONObject response) {
        try {
            JSONObject ok = new JSONObject();
            ok.put("success", true);
            ok.put("version", Checker.VERSION);
            if (response != null) {
                ok.put("response", response);
            }

            httpExchange.getResponseHeaders().add("Content-Type", "application/json");
            String answer = ok.toString();
            httpExchange.sendResponseHeaders(200, answer.getBytes(StandardCharsets.UTF_8).length);

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(answer.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

        } catch (IOException e) {
            log.error("Failed to set error code", e);
        }
    }

    public static boolean checkAuth(HttpExchange exchange) {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null) return false;
        token = token.substring("Bearer ".length());
        return token.equals(Checker.TOKEN);
    }

    public static Map<String, String> queryToMap(String query) {
        if(query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
