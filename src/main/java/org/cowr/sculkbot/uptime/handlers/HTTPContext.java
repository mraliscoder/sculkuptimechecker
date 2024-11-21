package org.cowr.sculkbot.uptime.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.cowr.sculkbot.uptime.Checker;
import org.cowr.sculkbot.uptime.Helper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

public class HTTPContext implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Helper.checkAuth(exchange)) {
            Helper.error(exchange, 403, "Unauthorized");
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                String input = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
                JSONObject obj = new JSONObject(input);

                if (!obj.has("url")) {
                    Helper.error(exchange, 400, "Some required fields are empty");
                    return;
                }

                boolean success = false;
                int code = -1;
                String error = "";

                long time1 = System.currentTimeMillis();

                try {
                    URL url = new URL(obj.getString("url"));
                    if (Objects.equals(url.getProtocol(), "http")) {
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestProperty("User-Agent", "SculkUptimeChecker/" + Checker.VERSION + " (Sculk Ltd.; http://sculk.ru/; me@edwardcode.net)");
                        connection.setConnectTimeout(3000);
                        connection.connect();

                        code = connection.getResponseCode();
                    } else if (url.getProtocol().equalsIgnoreCase("https")) {
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                        connection.setRequestProperty("User-Agent", "SculkUptimeChecker/" + Checker.VERSION + " (Sculk Ltd.; http://sculk.ru/; me@edwardcode.net)");
                        connection.setConnectTimeout(3000);
                        connection.connect();

                        code = connection.getResponseCode();
                    } else {
                        Helper.error(exchange, 400, "Invalid protocol");
                        return;
                    }
                    success = true;
                } catch (Throwable e) {
                    error = e.getMessage();
                }

                long time2 = System.currentTimeMillis();

                Helper.ok(exchange, new JSONObject()
                        .put("time", time2 - time1)
                        .put("code", code)
                        .put("result", success)
                        .put("error", error));
            } catch (Exception e) {
                Helper.error(exchange, 500, e.toString());
            }
        } else {
            Helper.noMethodExist(exchange);
        }
    }
}
