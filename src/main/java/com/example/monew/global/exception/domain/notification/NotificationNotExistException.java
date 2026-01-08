package com.example.monew.global.exception.domain.notification;

import com.example.monew.global.exception.CustomException;
import com.example.monew.global.exception.ErrorCode;
import java.util.HashMap;
import java.util.UUID;

public class NotificationNotExistException extends CustomException {
    public NotificationNotExistException(UUID notificationId, UUID userId) {
        super(ErrorCode.NOTIFICATION_NOT_EXIST, new HashMap<>(){
            {
                put("notificationId", notificationId);
                put("userId", userId);
            }
        });
    }
}
