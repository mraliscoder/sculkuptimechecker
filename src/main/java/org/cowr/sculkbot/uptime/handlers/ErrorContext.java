package org.cowr.sculkbot.uptime.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.cowr.sculkbot.uptime.Helper;

import java.io.IOException;

public class ErrorContext implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Helper.noMethodExist(exchange);
    }
}
