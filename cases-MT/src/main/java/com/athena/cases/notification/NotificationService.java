package com.athena.cases.notification;

import java.util.List;

/**
 * Port for team/user notifications related to cases.
 */
public interface NotificationService {

    void notifyTeam(NotifyTeamCommand command);

    void notifyUser(NotifyUserCommand command);

    List<NotificationQueueItem> listAuthorizedQueue();
}
