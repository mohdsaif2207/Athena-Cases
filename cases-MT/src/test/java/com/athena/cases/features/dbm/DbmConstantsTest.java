package com.athena.cases.features.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Contract tests for DBM notification / deep-link / transfer-type constants (US Scenarios 12–13).
 */
class DbmConstantsTest {

    @Test
    void should_buildNewCaseNotificationMessage_withCaseNumber() {
        assertThat(DbmConstants.newCaseNotificationMessage("DBM000042"))
                .isEqualTo("A new DBM Work Order Request (Case #DBM000042) has been assigned to your team.");
    }

    @Test
    void should_buildUpdatedCaseNotificationMessage_withCaseNumber() {
        assertThat(DbmConstants.updatedCaseNotificationMessage("DBM000042"))
                .isEqualTo("DBM Work Order Request (Case #DBM000042) has been updated.");
    }

    @Test
    void should_returnCasesDeepLink() {
        assertThat(DbmConstants.caseDeepLink(99L)).isEqualTo("/cases");
        assertThat(DbmConstants.CASE_DEEP_LINK).isEqualTo("/cases");
    }

    @Test
    void should_exposeWorkflowAndTeamContracts() {
        assertThat(DbmConstants.RECEIVER_TEAM_DBM).isEqualTo("DBM_TEAM");
        assertThat(DbmConstants.WORKFLOW_STATUS_PENDING_ASSIGNMENT).isEqualTo("Pending Assignment");
        assertThat(DbmConstants.CASE_TYPE_CODE).isEqualTo("DBM_WORK_ORDER_REQUEST");
        assertThat(DbmConstants.TRANSFER_TYPE_OTHER).isEqualTo("Other (Requires Description)");
        assertThat(DbmConstants.TRANSFER_TYPE_CUSTOM).isEqualTo("Custom");
        assertThat(DbmConstants.TRANSFER_TYPE_CUSTOM_REQUIRES_APPROVAL)
                .isEqualTo("Custom (Requires Approval)");
    }
}
