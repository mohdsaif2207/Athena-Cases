package com.athena.cases.filestorage;

public record StoredFileRef(String storageKey, String fileName, long sizeBytes) {
}
