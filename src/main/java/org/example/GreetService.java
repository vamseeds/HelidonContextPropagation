
package org.example;

import java.util.Collections;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;

import io.helidon.common.http.Http;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.media.common.MessageBodyWriterContext;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.multipart.MultiPartEncoder;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.media.multipart.ReadableMultiPart;
import io.helidon.media.multipart.WriteableBodyPart;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import io.helidon.webclient.WebClient;


/**
 * A simple service to greet you. Examples:
 * <p>
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 * <p>
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 * <p>
 * Change greeting
 * curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 * <p>
 * The message is returned as a JSON object
 */

public class GreetService implements Service {

    /**
     * The config value for the key {@code greeting}.
     */

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final Logger LOGGER = Logger.getLogger(GreetService.class.getName());

    GreetService(Config config) {
    }

    /**
     * A service registers itself by updating the routing rules.
     *
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        final var multipart = "/{ws}" + "/multipart";
        final var message = "/{ws}" + "/message";
        final var response = "/response";

        rules.post(multipart, this::processMultipartHandler);
        rules.post(message, this::processMessageHandler);
        rules.post(response, this::responseHandler);
    }

    private void responseHandler(ServerRequest serverRequest, ServerResponse serverResponse) {
        serverResponse.send("{ \"status\": \"Success\" }");
    }

    /**
     * Return a worldly greeting message.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void processMultipartHandler(ServerRequest request, ServerResponse response) {
        request.content().as(ReadableMultiPart.class)
                .map(multiPart -> {
                    LOGGER.info("Initial Namespace in processMultipartHandler map :" + NamespaceUtil.getNamespace());
                    return multiPart;
                })
                .flatMap(this::multipartWebclientCall)
                .first()
                .thenAccept(stringResponse ->
                {
                    LOGGER.info("Namespace post webclient call in processMultipartHandler accept :" + NamespaceUtil.getNamespace());
                    LOGGER.info(stringResponse);
                    response.send(stringResponse);
                }).exceptionallyAccept(response::send);
    }

    private Single<String> multipartWebclientCall(ReadableMultiPart multiPart) {
        LOGGER.info("Namespace in multipartWebclientCall :" + NamespaceUtil.getNamespace());
        String mediaType = MediaType.builder()
                .type(MediaType.MULTIPART_FORM_DATA.type())
                .subtype(MediaType.MULTIPART_FORM_DATA.subtype())
                .addParameter("boundary", "boundary")
                .build()
                .toString();
        final MessageBodyWriterContext EMPTY_WRITER_CONTEXT = MessageBodyWriterContext.create();
        MultiPartEncoder encoder = MultiPartEncoder.create("boundary", EMPTY_WRITER_CONTEXT);
        Single.just(writeableBodyPart(multiPart.bodyParts().get(0))).subscribe(encoder);
        return WebClient.builder().addMediaSupport(MultiPartSupport.create()).build().post().uri("http://localhost:8080/response")
                .addHeader(Http.Header.CONTENT_TYPE, mediaType)
                .submit(encoder)
                .flatMapSingle(webClientResponse -> webClientResponse.content().as(String.class));
    }

    protected WriteableBodyPart writeableBodyPart(ReadableBodyPart part) {
        return WriteableBodyPart.builder()
                .entity(part.as(byte[].class))
                .name(part.name())
                .filename(part.filename())
                .build();
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void processMessageHandler(ServerRequest request, ServerResponse response) {
        request.content().as(String.class)
                .map(s -> {
                    LOGGER.info("Initial Namespace in processMessageHandler map :" + NamespaceUtil.getNamespace());
                    return s;
                })
                .flatMap(this::webclientCall)
                .first()
                .thenAccept(stringResponse ->
                {
                    LOGGER.info("Namespace post Webclient Call in  processMessageHandler accept :" + NamespaceUtil.getNamespace());
                    LOGGER.info(stringResponse);
                    response.send(stringResponse);
                }).exceptionallyAccept(response::send);
    }

    private Single<String> webclientCall(String s) {
        LOGGER.info("Namespace in webclientCall :" + NamespaceUtil.getNamespace());

        return WebClient.builder().addMediaSupport(JsonpSupport.create()).build().post().uri("http://localhost:8080/response")
                .submit(s)
                .flatMapSingle(webClientResponse -> webClientResponse.content().as(String.class));
    }

}