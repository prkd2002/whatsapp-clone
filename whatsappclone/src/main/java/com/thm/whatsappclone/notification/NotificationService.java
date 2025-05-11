package com.thm.whatsappclone.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String userId, Notification notification) {
        log.info("Sending WS notification to user {} with payload {} ", userId,notification);
        messagingTemplate.convertAndSendToUser(userId,
                "/chat",
                notification);

    }

}
