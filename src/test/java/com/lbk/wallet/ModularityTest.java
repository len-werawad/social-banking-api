package com.lbk.wallet;


import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTest {

    @Test
    public void applicationModules() {
       ApplicationModules modules = ApplicationModules.of(WalletApplication.class);
        modules.forEach(System.out::println);
        modules.verify();

    }

    @Test
    void createDocumentation(){
        ApplicationModules modules = ApplicationModules.of(WalletApplication.class);
        new Documenter(modules).writeDocumentation();
    }
}
