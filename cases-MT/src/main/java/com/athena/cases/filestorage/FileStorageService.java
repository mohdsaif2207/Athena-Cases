package com.athena.cases.filestorage;

/**
 * Port for attachment storage. Adapters may use local disk (dev) or object storage (later).
 */
public interface FileStorageService {

    StoredFileRef store(StoreFileCommand command);

    byte[] load(String storageKey);

    void delete(String storageKey);
}
