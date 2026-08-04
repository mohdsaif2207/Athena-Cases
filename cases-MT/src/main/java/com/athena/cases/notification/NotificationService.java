package com.athena.cases.notification;

import java.util.List;

/**
 * Port for team/user notifications related to cases.
 */
public interface NotificationService {

    void notifyTeam(NotifyTeamCommand command);

    void notifyUser(NotifyUserCommand command);

    /** Queue rows visible to the caller's receiving teams (requires NOTIF_VIEW). */
    List<NotificationQueueItem> listAuthorizedQueue();

    /** Updates a queue row when the caller is on that notification's receiving team. */
    NotificationQueueItem updateQueueItem(Long notificationId, UpdateNotificationCommand command);
}
