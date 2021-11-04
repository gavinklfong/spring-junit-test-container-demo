package component.setup;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.ConfigurableApplicationContext;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockSetup {

    static WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

    public static void registerComponent(ConfigurableApplicationContext configurableApplicationContext) {
        configurableApplicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);
    }
    
    public static void setUp() {
        wireMockServer.start();
    }

    public static void tearDown() {
        wireMockServer.stop();
    }

    public static String getBaseUrl() {
        return wireMockServer.baseUrl();
    }

    public static void reset() { wireMockServer.resetAll(); }

}
