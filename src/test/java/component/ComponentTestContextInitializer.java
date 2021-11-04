package component;

import component.setup.TestContainersSetup;
import component.setup.WireMockSetup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static component.setup.TestContainersSetup.*;

@Slf4j
public class SpringBootContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        // start up and initialize test containers
        TestContainersSetup.initTestContainers(configurableApplicationContext.getEnvironment());

        // startup wiremock
        WireMockSetup.setUp();
        WireMockSetup.registerComponent(configurableApplicationContext);

        // alter spring boot system properties so that it will connect to test containers and wiremock
        TestPropertyValues values = TestPropertyValues.of(
                "spring.rabbitmq.host=" + getRabbitMQContainerIPAddress(),
                "spring.rabbitmq.port=" + getRabbitMQContainerPort(),
                "spring.data.mongodb.uri=" + getMongoDBContainerUri(),
                "app.customer-srv.url=" + WireMockSetup.getBaseUrl()
        );

        values.applyTo(configurableApplicationContext);

        log.info("======= Customized properties settings =======");
        log.info("spring.rabbitmq.host=" + getRabbitMQContainerIPAddress());
        log.info("spring.rabbitmq.port=" + getRabbitMQContainerPort());
        log.info("spring.data.mongodb.uri=" + getMongoDBContainerUri());
        log.info("app.customer-srv.url=" + WireMockSetup.getBaseUrl());
        log.info("==============");

    }
}
