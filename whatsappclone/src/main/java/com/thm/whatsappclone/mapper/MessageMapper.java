package com.thm.whatsappclone.mapper;

import com.thm.whatsappclone.message.Message;
import com.thm.whatsappclone.response.MessageResponse;
import com.thm.whatsappclone.utils.FileUtils;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .type(message.getType())
                .state(message.getState())
                .createdAt(message.getCreatedAt())
                .media(FileUtils.readFileFromLocation(message.getMediaFilePath()))
                //Todo read the media file
                .build();
    }
}
