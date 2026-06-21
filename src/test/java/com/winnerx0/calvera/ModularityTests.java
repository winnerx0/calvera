package com.winnerx0.calvera;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(CalveraApplication.class).verify();
    }
}
