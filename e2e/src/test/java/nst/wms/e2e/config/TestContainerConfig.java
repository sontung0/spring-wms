package nst.wms.e2e.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:lts")
            .withDatabaseName("wms")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return MYSQL;
    }
}
