package com.azuresdk.controller;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azuresdk.services.KeyVaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AzureController {
  private static final ClientLogger LOGGER = new ClientLogger(AzureController.class);

  @Autowired
  private KeyVaultService keyVaultService;

  @GetMapping(path = "/kv/sync")
  public String testSync() {
    try {
      keyVaultService
          .listKeys()
          .forEach(keyBase -> LOGGER.asInfo().log(keyBase.name()));
      return "Successfully ran all KeyVault Keys sync tests";
    } catch (Exception ex) {
      LOGGER.asError().log("Failed to run KeyVault sync tests", ex.getMessage());
      return "Failed to run KeyVault sync tests";
    }
  }

  @GetMapping(path = "/kv/async")
  public String testAsync() {
    try {
      keyVaultService
          .listKeysAsync()
          .doOnNext(keyBase -> LOGGER.asInfo().log(keyBase.name()))
          .subscribe();
      return "Successfully ran all KeyVault Keys async tests";
    } catch (Exception ex) {
      LOGGER.asError().log("Failed to run KeyVault async tests", ex.getMessage());
      return "Failed to run KeyVault async tests " + ex.getMessage();
    }
  }
}
