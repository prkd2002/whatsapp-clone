package com.thm.whatsappclone.services;

import com.thm.whatsappclone.chat.Chat;
import com.thm.whatsappclone.mapper.ChatMapper;
import com.thm.whatsappclone.repository.ChatRepository;
import com.thm.whatsappclone.repository.UserRepository;
import com.thm.whatsappclone.response.ChatResponse;
import com.thm.whatsappclone.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByReceiverId(Authentication currentUser){
        final String userId = currentUser.getName();
        return chatRepository.findChatsBySenderId(userId)
                .stream()
                .map(c -> chatMapper.toChatResponse(c, userId))
                .toList();

    }

    public String createChat(String senderId, String receiverId){
        Optional<Chat> existingChat = chatRepository.findChatByReceiverAndSenderId(senderId, receiverId);
        if(existingChat.isPresent()){
            return existingChat.get().getId();
        }
        User sender = userRepository.findByPublicId(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender with id " +  senderId +   " not found"))   ;

        User receiver = userRepository.findByPublicId(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("ReceiverId with id " +  senderId +   " not found"))   ;

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);

        Chat savedChat = chatRepository.save(chat);
        return savedChat.getId();

    }
}

