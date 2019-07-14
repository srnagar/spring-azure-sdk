package com.azuresdk.services;


import com.azure.security.keyvault.keys.models.KeyBase;
import reactor.core.publisher.Flux;

public interface KeyVaultService {

  Iterable<KeyBase> listKeys();

  Flux<KeyBase> listKeysAsync();

}
