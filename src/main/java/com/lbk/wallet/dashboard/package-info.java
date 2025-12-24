@org.springframework.modulith.ApplicationModule(
        displayName = "Dashboard Module",
        allowedDependencies = {"common :: api", "auth :: event", "customer :: api", "account :: api", "account :: dto"}
)
package com.lbk.wallet.dashboard;

