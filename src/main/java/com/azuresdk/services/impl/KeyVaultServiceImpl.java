package com.azuresdk.services.impl;

import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azuresdk.services.KeyVaultService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class KeyVaultServiceImpl implements KeyVaultService {

  @Override
  public Iterable<KeyBase> listKeys() {
    KeyClient keyClient = KeyClient.builder()
        .credential(new DefaultAzureCredential())
        .endpoint("https://mykeyvault.vault.azure.net/")
        .build();
    return keyClient.listKeys();
  }

  @Override
  public Flux<KeyBase> listKeysAsync() {
    KeyAsyncClient keyAsyncClient = KeyAsyncClient.builder()
        .credential(new DefaultAzureCredential())
        .endpoint("https://mykeyvault.vault.azure.net/")
        .build();
    return keyAsyncClient.listKeys();
  }

}
