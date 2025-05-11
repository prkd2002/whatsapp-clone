package com.thm.whatsappclone.message;

import com.thm.whatsappclone.chat.Chat;
import com.thm.whatsappclone.common.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="messages")
@NamedQuery(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID, query =" SELECT m FROM Message m WHERE m.chat.id  = :chatId ORDER BY m.createdAt  ")
@NamedQuery(name = MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT, query = "UPDATE Message SET state = :newState WHERE chat.id = :chatId")
public class Message extends BaseAuditingEntity {
    @Id
    @SequenceGenerator(name = "msg_seq", sequenceName = "msg_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_seq")
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    private MessageState state;
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="chat_id", nullable = false)
    private Chat chat;

    @Column(name="sender_id", nullable = false)
    private String senderId;
    @Column(name="receiver_id", nullable = false)
    private String receiverId;

    private String mediaFilePath;

}
