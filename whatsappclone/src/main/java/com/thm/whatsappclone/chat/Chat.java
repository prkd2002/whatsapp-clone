package com.thm.whatsappclone.chat;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.thm.whatsappclone.common.BaseAuditingEntity;
import com.thm.whatsappclone.message.Message;
import com.thm.whatsappclone.message.MessageState;
import com.thm.whatsappclone.message.MessageType;
import com.thm.whatsappclone.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="chats")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID, query = "SELECT c FROM Chat c WHERE c.sender.id = :senderId OR c.receiver.id = :senderId ORDER BY createdAt DESC")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER_ID, query = "SELECT DISTINCT c FROM Chat c  WHERE (c.sender.id = :senderId AND c.receiver.id = :receiverId ) OR ( c.sender.id = :receiverId AND c.receiver.id = :receiverId )" )
public class Chat extends BaseAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="receiver_id", nullable = false)
    private User receiver;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
    @OrderBy("createdAt DESC")
    private List<Message> messages;

    @Transient
    public String getChatName(final String senderId ){
        if(receiver.getId().equals(senderId)){
            return sender.getFirstName() + " " + sender.getLastName();
        }

        return receiver.getFirstName() + " " + receiver.getLastName();
    }

    @Transient
    public Long getUnreadMessages(final String senderId){
        return messages
                .stream()
                .filter(m -> m.getReceiverId().equals(senderId))
                .filter(m -> MessageState.SENT == m.getState())
                .count();

    }


    @Transient
    public String getLastMessage(){
        if(messages != null && !messages.isEmpty()){
            if(messages.get(0).getType() == MessageType.TEXT){
                return "Attachment";
            }
            return messages.get(0).getContent();
        }

        return "";
    }



    @Transient
    public LocalDateTime getLastMessageTime(){
        if(messages != null && !messages.isEmpty()){
            return messages.get(0).getCreatedAt();
        }
        return null;
    }
}
