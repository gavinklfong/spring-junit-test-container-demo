package integration;

import integration.setup.TestContainersSetup;
import integration.setup.WireMockSetup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static integration.setup.TestContainersSetup.*;

@Slf4j
public class SpringBootContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        TestContainersSetup.initTestContainers(configurableApplicationContext.getEnvironment());

        WireMockSetup.setUp();
        WireMockSetup.registerComponent(configurableApplicationContext);

        TestPropertyValues values = TestPropertyValues.of(
                "spring.rabbitmq.host=" + getRabbitMQContainerIPAddress(),
                "spring.rabbitmq.port=" + getRabbitMQContainerPort(),
                "test-containers.rabbitmq.management.address=" + getRabbitMQContainerIPAddress() + ":" + getRabbitMQContainerManagementPort(),
                "spring.data.mongodb.uri=" + getMongoDBContainerUri(),
                "app.customer-srv.url=" + WireMockSetup.getBaseUrl()
        );

        log.info("spring.rabbitmq.host=" + getRabbitMQContainerIPAddress());
        log.info("spring.rabbitmq.port=" + getRabbitMQContainerPort());
        log.info("spring.data.mongodb.uri=" + getMongoDBContainerUri());
        log.info("app.customer-srv.url=" + WireMockSetup.getBaseUrl());

        values.applyTo(configurableApplicationContext);
    }
}
