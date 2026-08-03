package com.athena.cases.features.exrt.util;

import com.athena.cases.casemanagement.CaseNumberGenerator;
import org.springframework.stereotype.Component;

/**
 * ExRT-specific case number: {@code ExR} + 6-digit zero-padded sequence from {@code exrt_case_number_seq}.
 */
@Component
public class ExrtCaseNumberGenerator {

    public static final String SEQUENCE_NAME = "exrt_case_number_seq";
    public static final String PREFIX = "ExR";
    public static final int PAD_WIDTH = 6;

    private final CaseNumberGenerator caseNumberGenerator;

    public ExrtCaseNumberGenerator(CaseNumberGenerator caseNumberGenerator) {
        this.caseNumberGenerator = caseNumberGenerator;
    }

    public String next() {
        return caseNumberGenerator.next(SEQUENCE_NAME, PREFIX, PAD_WIDTH);
    }
}
