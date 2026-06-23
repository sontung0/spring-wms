package nst.wms.e2e;

import nst.wms.e2e.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfig.class)
@ActiveProfiles("e2e")
public abstract class AbstractE2eTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // No special setup needed — TestRestTemplate is auto-configured
    }
}
