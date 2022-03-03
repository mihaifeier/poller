package com.kry.poller.verticle;

import com.kry.poller.service.GatewayService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GatewayVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(GatewayVerticle.class);
    private Map<String, String> handlersByUser;
    private Router router;
    private GatewayService gatewayService;

    @Override
    public void start(Promise<Void> startPromise) {
        gatewayService = new GatewayService(vertx);
        handlersByUser = new HashMap<>();

        HttpServer server = vertx.createHttpServer();
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        setCORS();
        configureSockJs();

        router.get("/poll").handler(message -> gatewayService.getPolls(message));
        router.post("/poll").handler(message -> gatewayService.addPoll(message));
        router.put("/poll").handler(message -> gatewayService.updatePoll(message));
        router.delete("/poll/:id").handler(message -> gatewayService.deletePoll(message));

        server.requestHandler(router).listen(8080);
        logger.info("Gateway Verticle started successfully.");

        startPromise.complete();
    }

    private void configureSockJs() {
        SockJSHandlerOptions options = new SockJSHandlerOptions().setRegisterWriteHandler(true);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        router.mountSubRouter("/live-update", sockJSHandler.socketHandler(sockJSSocket ->
            sockJSSocket.handler(buffer ->
                gatewayService.handleSockJs(buffer.toString(), sockJSSocket.writeHandlerID()))));

        vertx.eventBus().<JsonObject>consumer(
            "polling.live-update",
            message -> gatewayService.handleLiveUpdateMessage(message.body())
        );
    }

    private void setCORS() {
        router.route().handler(CorsHandler.create("*")
            .allowedMethods(Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
            .addOrigin("http://localhost:3000")
            .allowCredentials(true)
        );
    }
}
