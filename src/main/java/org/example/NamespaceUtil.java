package org.example;

import io.helidon.logging.common.HelidonMdc;

public class NamespaceUtil {
    static final String MDC_NAMESPACE_KEY = "namespace-util-namespace-key";

    static String apiGatewayBaseURL = "";

    public static void setNamespace(final String namespace) {
        System.out.println("Setting Namespace : " + namespace);
        HelidonMdc.set(MDC_NAMESPACE_KEY, namespace);
        System.out.println("MDC_NAMESPACE_KEY After set: " + HelidonMdc.get(MDC_NAMESPACE_KEY));
    }

    public static String getNamespace() {
        System.out.println("MDC_NAMESPACE_KEY: " + HelidonMdc.get(MDC_NAMESPACE_KEY));
        return HelidonMdc.get(MDC_NAMESPACE_KEY)
                .orElse("Default-test");
    }

    public static NamespaceUtil getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static void setApiGatewayBaseURL(String s) {
        apiGatewayBaseURL = s;
    }

    public static String getApiGatewayBaseURL() {
        return apiGatewayBaseURL;
    }

    private static class LazyHolder {
        static final NamespaceUtil INSTANCE = new NamespaceUtil();
    }

    private String kubernetesNamespace;

    @SuppressWarnings("initialization.fields.uninitialized")
    private NamespaceUtil() {
    }

    public String getRequestNamespace() {
        return HelidonMdc.get(MDC_NAMESPACE_KEY).orElse(null);
    }
}
