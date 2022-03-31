package org.example;

import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import java.util.function.Supplier;

public class NamespaceService implements Service {
    private static final String NAMESPACE = "namespace";

    static final Supplier<NamespaceService> DEFAULT_SUPPLIER_FUNCTION = NamespaceService::new;
    private static Supplier<NamespaceService> supplierFunction = DEFAULT_SUPPLIER_FUNCTION;

    private static NamespaceService theInstance = null;


    private NamespaceService() {

    }

    /**
     * Factory method. Creates a singleton.
     *
     * @return the instance
     */
    public static NamespaceService getInstance() {
        if (theInstance == null) {
            theInstance = supplierFunction.get();
        }
        return theInstance;
    }

    @Override
    public void update(final Routing.Rules rules) {
        final var pathPattern = String.format("/{%s}/{:.*}", NAMESPACE);
        rules.any(pathPattern, this::processRequest);
    }

    private void processRequest(final ServerRequest serverRequest, final ServerResponse serverResponse) {
        if (setupNamespace(serverRequest)) {
            serverRequest.next();
        } else {
            final var msg = "Namespace not found for specified path parameter.";
            serverRequest.next(new RuntimeException(msg));
        }
    }

    /**
     * Set the current namespace again in a separate thread, e.g. WebClient response handler
     *
     * @param serverRequest
     * @return true if successful
     */
    public boolean setupNamespace(final ServerRequest serverRequest) {
        final var namespaceName = serverRequest.path().param(NAMESPACE);
        NamespaceUtil.setNamespace(namespaceName);
        return true;
    }

    // For unit testing
    public static void setSupplierFunction(Supplier<NamespaceService> supplierFunction) {
        theInstance = null;
        NamespaceService.supplierFunction = supplierFunction;
    }
}
