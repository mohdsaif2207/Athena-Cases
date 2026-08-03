package com.athena.cases.notification;

/**
 * Port for team/user notifications related to cases.
 */
public interface NotificationService {

    void notifyTeam(NotifyTeamCommand command);

    void notifyUser(NotifyUserCommand command);

    /** Queue rows visible to the caller's receiving teams (requires NOTIF_VIEW). */
    java.util.List<NotificationQueueItem> listAuthorizedQueue();
}
