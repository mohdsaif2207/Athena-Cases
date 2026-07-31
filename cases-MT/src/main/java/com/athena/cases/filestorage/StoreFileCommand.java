package com.athena.cases.filestorage;

public record StoreFileCommand(
        String fileName,
        String contentType,
        byte[] content,
        String caseTypeCode,
        Long caseId
) {
}
