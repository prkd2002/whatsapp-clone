package com.thm.whatsappclone.mapper;

import com.thm.whatsappclone.chat.Chat;
import com.thm.whatsappclone.response.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatMapper {
    public ChatResponse toChatResponse(Chat c, String userId) {
        return ChatResponse.builder()
                .chatId(c.getId())
                .name(c.getChatName(userId))
                .unreadCount(c.getUnreadMessages(userId))
                .lastMessage(c.getLastMessage())
                .isReceiverOnline(c.getReceiver().isUserOnline())
                .receiverId(c.getReceiver().getId())
                .senderId(c.getSender().getId())
                .lastMessageTime(c.getLastMessageTime())
                .build()
                ;
    }
}
