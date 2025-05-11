package com.thm.whatsappclone.repository;

import com.thm.whatsappclone.chat.Chat;
import com.thm.whatsappclone.chat.ChatConstants;
import com.thm.whatsappclone.response.ChatResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
    @Query(name = ChatConstants.FIND_CHAT_BY_SENDER_ID)
    List<Chat> findChatsBySenderId(@Param("senderId")String userId);

    @Query(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER_ID)
    Optional<Chat> findChatByReceiverAndSenderId(@Param("senderId")String senderId, @Param("receiverId")String receiverId);
}
