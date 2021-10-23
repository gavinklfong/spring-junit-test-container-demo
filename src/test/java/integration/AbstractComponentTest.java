package integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import space.gavinklfong.demo.insurance.InsuranceApplication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        initializers = { SpringBootContextInitializer.class },
        classes = {InsuranceApplication.class, ComponentTestContextConfig.class}
)
@ActiveProfiles(profiles={"component-test"})
public abstract class AbstractComponentTest {
}
