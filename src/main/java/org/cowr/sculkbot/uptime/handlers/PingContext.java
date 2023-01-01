package org.cowr.sculkbot.uptime.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.cowr.sculkbot.uptime.Helper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class PingContext implements HttpHandler {
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

                if (!obj.has("host")) {
                    Helper.error(exchange, 400, "Some required fields are empty");
                    return;
                }

                InetAddress address = InetAddress.getByName(obj.getString("host"));
                JSONArray resultsJson = new JSONArray();

                int success = 0;
                List<Integer> pings = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    long time1 = System.currentTimeMillis();
                    boolean available = address.isReachable(3000);
                    long time2 = System.currentTimeMillis();

                    JSONObject pingItem = new JSONObject();
                    pingItem.put("attempt", i + 1);
                    pingItem.put("available", available);
                    if (available) {
                        success = success + 1;
                        pingItem.put("ping", time2 - time1);
                        pings.add((int) (time2 - time1));
                    }

                    resultsJson.put(pingItem);
                }

                OptionalDouble average = pings.stream().mapToDouble(a -> a).average();
                int av = average.isPresent() ? (int) average.getAsDouble() : 0;

                Helper.ok(exchange, new JSONObject()
                        .put("result", success > 0)
                        .put("percent", (0.25 * success) * 100)
                        .put("attempts", resultsJson)
                        .put("average", av));
            } catch (Exception e) {
                Helper.error(exchange, 500, e.toString());
            }
        } else {
            Helper.noMethodExist(exchange);
        }
    }
}
