package nst.wms;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithVerificationTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(WmsApplication.class).verify();
    }
}
