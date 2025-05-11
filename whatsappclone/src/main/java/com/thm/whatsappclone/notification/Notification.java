package com.thm.whatsappclone.notification;


import com.thm.whatsappclone.message.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private String chatId;
    private String content;
    private String senderId;
    private String receiverId;
    private String chatName;
    private MessageType messageType;
    private NotificationType notificationType;
    private byte[] media;

}
