package com.athena.cases.features.exrt.service;

import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateResponse;
import com.athena.cases.features.exrt.dto.ExrtCaseDetailsResponse;
import com.athena.cases.features.exrt.dto.ExrtLookupItemResponse;
import java.util.List;

public interface ExrtCaseService {

    ExrtCaseCreateResponse create(ExrtCaseCreateRequest request);

    ExrtCaseDetailsResponse getById(Long caseId);

    List<ExrtLookupItemResponse> listStaticLookups(String lookupType);

    List<ExrtLookupItemResponse> listCarriers();

    List<ExrtLookupItemResponse> listTierIiAgents();

    List<ExrtLookupItemResponse> listAssignees();

    List<ExrtLookupItemResponse> listContacts(String clientId);

    List<ExrtLookupItemResponse> listReasonCodes();
}
