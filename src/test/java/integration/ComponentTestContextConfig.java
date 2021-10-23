package integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"space.gavinklfong.demo.insurance", "integration"})
public class ComponentTestContextConfig {

}
