package com.athena.cases.features.exrt.mapper;

import com.athena.cases.common.enums.CaseTypeCode;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateRequest;
import com.athena.cases.features.exrt.dto.ExrtCaseCreateResponse;
import com.athena.cases.features.exrt.dto.ExrtCaseDetailsResponse;
import com.athena.cases.features.exrt.entity.CaseHeaderEntity;
import com.athena.cases.features.exrt.entity.ExrtCaseDetailEntity;
import org.springframework.stereotype.Component;

@Component
public class ExrtCaseMapper {

    public CaseHeaderEntity toHeader(ExrtCaseCreateRequest request, String caseOwner, String actor) {
        CaseHeaderEntity header = new CaseHeaderEntity();
        header.setCaseType(CaseTypeCode.EXRT_REQUEST.name());
        header.setClientId(request.clientId());
        header.setSubject(request.subject());
        header.setDescription(request.description());
        header.setCaseOwner(caseOwner);
        header.setStatus(blankToDefault(request.status(), "Requested - ExRT"));
        header.setPriority(blankToDefault(request.priority(), "MEDIUM").toUpperCase());
        header.setWorkflowTriggered(false);
        header.setCreatedBy(actor);
        header.setUpdatedBy(actor);
        return header;
    }

    public ExrtCaseDetailEntity toDetail(ExrtCaseCreateRequest request, CaseHeaderEntity header, String actor) {
        ExrtCaseDetailEntity detail = new ExrtCaseDetailEntity();
        detail.setCaseHeader(header);
        detail.setFirstName(request.firstName());
        detail.setLastName(request.lastName());
        detail.setMi(request.mi());
        detail.setState(request.state());
        detail.setPhoneNumber(emptyToNull(request.phoneNumber()));
        detail.setProductId(request.productId());
        detail.setCoverageId(emptyToNull(request.coverageId()));
        detail.setCarrierId(request.carrierId());
        detail.setCustomerContactEmail(emptyToNull(request.customerContactEmail()));
        detail.setTierIiAgentId(emptyToNull(request.tierIiAgentId()));
        detail.setExrtType(request.type());
        detail.setPolicyNumber(emptyToNull(request.policyNumber()));
        detail.setDisposition(request.disposition());
        detail.setInquirySource(request.inquirySource());
        detail.setActionNeeded(request.actionNeeded());
        detail.setRequestAssignedTo(emptyToNull(request.requestAssignedTo()));
        detail.setReasonForEscalation(request.reasonForEscalation());
        detail.setReasonCode1(request.reasonCode1());
        detail.setRequestorNotes(emptyToNull(request.requestorNotes()));
        detail.setNotesIssues(emptyToNull(request.notesIssues()));
        detail.setCoachingFeedback(emptyToNull(request.coachingFeedback()));
        detail.setCallCenterEducation(Boolean.TRUE.equals(request.callCenterEducation()));
        detail.setCaseOrigin(request.caseOrigin());
        detail.setWebMail(emptyToNull(request.webMail()));
        detail.setContactName(emptyToNull(request.contactName()));
        detail.setCreatedBy(actor);
        detail.setUpdatedBy(actor);
        return detail;
    }

    public ExrtCaseCreateResponse toCreateResponse(CaseHeaderEntity header) {
        return new ExrtCaseCreateResponse(
                header.getId(),
                header.getCaseNumber(),
                header.getCaseType(),
                header.getStatus(),
                header.getCaseOwner(),
                "Case created successfully with Case ID " + header.getCaseNumber() + "."
        );
    }

    public ExrtCaseDetailsResponse toDetailsResponse(CaseHeaderEntity header, ExrtCaseDetailEntity detail) {
        return new ExrtCaseDetailsResponse(
                header.getId(),
                header.getCaseNumber(),
                header.getCaseType(),
                header.getStatus(),
                header.getPriority(),
                header.getCaseOwner(),
                header.getClientId(),
                header.getSubject(),
                header.getDescription(),
                detail.getFirstName(),
                detail.getLastName(),
                detail.getMi(),
                detail.getState(),
                detail.getPhoneNumber(),
                detail.getProductId(),
                detail.getCoverageId(),
                detail.getCarrierId(),
                detail.getCustomerContactEmail(),
                detail.getTierIiAgentId(),
                detail.getExrtType(),
                detail.getPolicyNumber(),
                detail.getDisposition(),
                detail.getInquirySource(),
                detail.getActionNeeded(),
                detail.getRequestAssignedTo(),
                detail.getReasonForEscalation(),
                detail.getReasonCode1(),
                detail.getRequestorNotes(),
                detail.getNotesIssues(),
                detail.getCoachingFeedback(),
                detail.isCallCenterEducation(),
                detail.getCaseOrigin(),
                detail.getWebMail(),
                detail.getContactName(),
                header.isWorkflowTriggered()
        );
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
