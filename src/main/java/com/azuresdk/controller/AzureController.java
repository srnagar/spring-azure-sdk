package com.azuresdk.controller;

import com.azuresdk.services.KeyVaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AzureController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AzureController.class);

  @Autowired
  private KeyVaultService keyVaultService;

  @GetMapping(path = "/key_vault/list_keys")
  public String listKeys(@RequestParam(value = "async", required = false, defaultValue = "false") boolean isAsync) {
    LOGGER.info("Listing KeyVault keys using " + (isAsync ? "async" : "sync") + " key client");
    try {
      StringBuilder sb = new StringBuilder();
      if (isAsync) {
        keyVaultService
            .listKeysAsync()
            .doOnNext(keyBase -> sb.append(keyBase.name()).append(", "))
            .subscribe();
      } else {
        keyVaultService
            .listKeys()
            .forEach(keyBase -> sb.append(keyBase.name()).append(", "));
      }
      LOGGER.info("Successfully listed KeyVault keys");
      return sb.deleteCharAt(sb.length() - 1).toString();
    } catch (Exception ex) {
      LOGGER.error("Failed to list KeyVault keys", ex.getMessage());
      return "Failed to list KeyVault keys";
    }
  }
}
