package com.athena.cases.idallocation;

import com.athena.cases.common.exception.ResourceNotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Allocates display IDs from {@code business_case_id_sequences} under a pessimistic row lock.
 */
@Service
public class BusinessCaseIdServiceImpl implements BusinessCaseIdService {

    private static final Logger log = LoggerFactory.getLogger(BusinessCaseIdServiceImpl.class);

    private final BusinessCaseIdSequenceRepository sequenceRepository;

    public BusinessCaseIdServiceImpl(BusinessCaseIdSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public String allocate(String caseTypeCode) {
        if (caseTypeCode == null || caseTypeCode.isBlank()) {
            throw new IllegalArgumentException("caseTypeCode is required");
        }

        BusinessCaseIdSequenceEntity sequence = sequenceRepository
                .findByCaseTypeCodeForUpdate(caseTypeCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("BusinessCaseIdSequence", caseTypeCode));

        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        sequence.setUpdatedAt(Instant.now());
        sequenceRepository.save(sequence);

        String formatted = format(sequence.getPrefix(), sequence.getWidth(), value);
        log.info("business case id allocated - caseTypeCode={} businessCaseId={}", caseTypeCode, formatted);
        return formatted;
    }

    static String format(String prefix, int width, long value) {
        return prefix + String.format("%0" + width + "d", value);
    }
}
