package com.thm.whatsappclone.services;

import com.thm.whatsappclone.chat.Chat;
import com.thm.whatsappclone.mapper.MessageMapper;
import com.thm.whatsappclone.message.Message;
import com.thm.whatsappclone.message.MessageState;
import com.thm.whatsappclone.message.MessageType;
import com.thm.whatsappclone.notification.Notification;
import com.thm.whatsappclone.notification.NotificationService;
import com.thm.whatsappclone.notification.NotificationType;
import com.thm.whatsappclone.repository.ChatRepository;
import com.thm.whatsappclone.repository.MessageRepository;
import com.thm.whatsappclone.request.MessageRequest;
import com.thm.whatsappclone.response.MessageResponse;
import com.thm.whatsappclone.utils.FileUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;
    private final FileService fileService;
    private final NotificationService notificationService;

    public void saveMessage(MessageRequest messageRequest){
        Chat chat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat with id " + messageRequest.getChatId() + " not found"));

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        //message.setState(MessageState.SENT);


        messageRepository.save(message);

        // Todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(messageRequest.getType())
                .content(messageRequest.getContent())
                .senderId(messageRequest.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .notificationType(NotificationType.MESSAGE)
                .chatName(chat.getChatName(message.getSenderId()))
                .build();

        notificationService.sendNotification(message.getReceiverId(), notification);

    }


    public List<MessageResponse> findChatMessages(String chatId ){
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(messageMapper::toMessageResponse)
                .toList();


    }


    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication){
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat with id " + chatId + " not found"));

       final String receiverId = getReceiverId(chat, authentication);

        messageRepository.setMessagesToSeenByChatId(chatId,MessageState.SEEN);

        // todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(getSenderId(chat, authentication))
                .receiverId(receiverId)
                .notificationType(NotificationType.SEEN)
                .build();

        notificationService.sendNotification(receiverId, notification);
    }


    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication){
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat with id " + chatId + " not found"));

        final String senderId = getSenderId(chat, authentication);
        final String receiverId = getReceiverId(chat, authentication);

        final String filePath = fileService.saveFile(file,senderId );

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);
        messageRepository.save(message);


        // todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(MessageType.IMAGE)
                .senderId(senderId)
                .receiverId(receiverId)
                .notificationType(NotificationType.IMAGE)
                .media(FileUtils.readFileFromLocation(filePath))
                .build();

        notificationService.sendNotification(receiverId, notification);

    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if(chat.getSender().getId().equals(authentication.getName())){
            return chat.getSender().getId();
        }

        return chat.getReceiver().getId();
    }

    private String getReceiverId(Chat chat, Authentication authentication) {
        if(chat.getSender().getId().equals(authentication.getName())){
            return chat.getReceiver().getId();
        }

        return chat.getSender().getId();
    }
}
