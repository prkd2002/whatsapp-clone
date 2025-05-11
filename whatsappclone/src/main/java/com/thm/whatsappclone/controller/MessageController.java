package com.thm.whatsappclone.controller;

import com.thm.whatsappclone.request.MessageRequest;
import com.thm.whatsappclone.response.MessageResponse;
import com.thm.whatsappclone.services.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message Controller", description = "Operations pertaining to messages")
public class MessageController {

    private final MessageService messageService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveMessage(@RequestBody MessageRequest messageRequest){
        messageService.saveMessage(messageRequest);
    }


    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadMedia(
            @RequestParam("chat-id") String chatId,
            @Parameter()
                    @RequestParam("file") MultipartFile file,
            Authentication authentication
    ){
        messageService.uploadMediaMessage(chatId, file, authentication);
    }


    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void setMessagesToSeen(@RequestParam("chat-id") String chatId, Authentication authentication){
        messageService.setMessagesToSeen(chatId, authentication);
    }


    @GetMapping("/chat/{chat-id}")
    public ResponseEntity<List<MessageResponse>> getAllMessages(@PathVariable("chat-id") String chatId){
        return ResponseEntity.ok(messageService.findChatMessages(chatId));
    }


}
