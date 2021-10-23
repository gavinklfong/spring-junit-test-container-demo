package component;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"space.gavinklfong.demo.insurance", "component"})
public class ComponentTestContextConfig {

}
