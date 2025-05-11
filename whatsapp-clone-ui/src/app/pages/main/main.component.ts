import {AfterViewChecked, Component, ElementRef, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ChatListComponent} from '../../components/chat-list/chat-list.component';
import {ChatResponse} from '../../services/models/chat-response';
import {ChatControllerService, MessageControllerService} from '../../services/services';
import {KeycloakService} from '../../utils/keycloak/keycloak.service';
import {MessageResponse} from '../../services/models/message-response';
import {DatePipe} from '@angular/common';
import {PickerComponent} from '@ctrl/ngx-emoji-mart';
import {FormsModule} from '@angular/forms';
import {EmojiData} from '@ctrl/ngx-emoji-mart/ngx-emoji';
import {MessageRequest} from '../../services/models/message-request';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {Notification} from '../../models/notification.model';



@Component({
  selector: 'app-main',
  imports: [
    ChatListComponent,
    DatePipe,
    PickerComponent,
    FormsModule

  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements  OnInit, OnDestroy, AfterViewChecked{
  chats: Array<ChatResponse> = [];
  chatService = inject(ChatControllerService);
  keycloakService = inject(KeycloakService);
  selectedChat:ChatResponse = {};
  messageService = inject(MessageControllerService);
  chatMessages: Array<MessageResponse> = [];
  showEmojis: boolean = false;
  messageContent: string = '';
  socketClient: any = null;
  @ViewChild('scrolllableDiv') scrollableDiv!: ElementRef<HTMLDivElement>;
  private notificationSubscription: any;

  ngOnInit(): void {
    this.initWebSockets();
    this.getAllChats();
    //console.log(this.selectedChat)
  }


  private getAllChats() {
    this.chatService.getChatsByReceiver()
    .subscribe({
      next: (resp) => {
        console.log("chats",resp);
      this.chats = resp;
    },
      error: (err) => {
        console.log(err);
      }
    });
  }

  logout() {
    this.keycloakService.logout();

  }

  userProfile() {
    this.keycloakService.accountManagement();

  }

  chatSelected(chatResponse: ChatResponse) {
    this.selectedChat = chatResponse;
    this.getAllChatMessages(chatResponse.chatId as string);
    this.setMessagesToSeen();
    this.selectedChat.unreadCount = 0;


  }

  private getAllChatMessages(chatId: string) {
    //console.log('ChatId',chatId);
    this.messageService.getAllMessages({
      'chat-id': chatId as string,
    }).subscribe({
      next: (resp) => {
        console.log(resp);
        this.chatMessages = resp;
      },
      error: (err) => {
        console.log('getAllChatMessages',err);
      }
    })

  }

  private setMessagesToSeen() {
    this.messageService.setMessagesToSeen({
      'chat-id': this.selectedChat.chatId as string
    }).subscribe({
      next: (resp) => {
        console.log(resp);
      },
      error: (err) => {
        console.log('SetMessagesToSeen',err);
      }
    });

  }

  isSelfMessage(message: MessageResponse) {
    return message.senderId === this.keycloakService.userId;
  }


  uploadMedia(target: EventTarget | null) {
    const file = this.extractFileFromTarget(target);
    if(file !== null){
      const reader = new FileReader();
      reader.onload = () => {
        if(reader.result){
          const mediaLines = reader.result.toString().split(',')[1];

          this.messageService.uploadMedia({
            'chat-id': this.selectedChat.chatId as string,
            body: {
              file: file
            }
          }).subscribe({
            next: () => {
              const message: MessageResponse ={
                senderId: this.getSenderId(),
                receiverId: this.getReceiverId(),
                content: 'Attachment',
                type: 'IMAGE',
                media: [mediaLines],
                createdAt: new Date().toString()

              };

              this.chatMessages.push(message);

            },
            error: (err) => {
              console.log(err);
            }
          });
        }
      }
      reader.readAsDataURL(file);
    }


  }

  onSelectEmojis(emojiSelected: any) {
    const emoji: EmojiData = emojiSelected.emoji;
    this.messageContent += emoji.native;

  }



  keyDown(event: KeyboardEvent) {
    if(event.key === 'Enter'){
      this.sendMessage();
    }

  }

  onClick() {
    this.setMessagesToSeen();

  }

  sendMessage() {
    if(this.messageContent.trim().length > 0){
      console.log("ChatId von sendMessage",this.selectedChat.chatId);
      const messageRequest: MessageRequest = {
        chatId:  this.selectedChat.chatId ,
        senderId: this.getSenderId(),
        receiverId: this.getReceiverId(),
        content: this.messageContent,
        type: 'TEXT'
      };

      console.log("messageRequest",messageRequest);
      this.messageService.saveMessage({
        body: messageRequest
      }).subscribe({
        next: () => {
          const message: MessageResponse = {
            senderId: this.getSenderId(),
            receiverId: this.getReceiverId(),
            content: this.messageContent,
            type: 'TEXT',
            state: 'SENT',
            createdAt: new Date().toString()
          };

          this.selectedChat.lastMessage = this.messageContent;
          this.chatMessages.push(message);
          this.messageContent = '';
          this.showEmojis = false;


        },
        error: (err) => {
          console.log(err);
        }
      })


    }

  }


  private getSenderId() {
    if(this.selectedChat.senderId === this.keycloakService.userId){
      return this.selectedChat.senderId as string;
    }

    return this.selectedChat.receiverId as string;
  }


  private getReceiverId(){
    if(this.selectedChat.senderId === this.keycloakService.userId){
      return this.selectedChat.receiverId as string;
    }

    return this.selectedChat.senderId as string;
  }
  private initWebSockets() {
    const tokenParsed = this.keycloakService.keycloak.tokenParsed;
    const token = this.keycloakService.keycloak.token;

    if (tokenParsed?.sub) {
      const subUrl = `/user/${tokenParsed.sub}/chat`;

      this.socketClient = new Client({
        webSocketFactory: () => new SockJS('http://whatsapp-api:8080/ws'),
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },
        debug: (str) => console.log(str), // tu peux désactiver en production
        reconnectDelay: 5000, // tentative de reconnexion
      });

      this.socketClient.onConnect = () => {
        this.notificationSubscription = this.socketClient.subscribe(subUrl, (message: IMessage) => {
          const notification: Notification = JSON.parse(message.body);
          this.handleNotification(notification);
        });
      };

      this.socketClient.onStompError = (frame: any) => {
        console.error('STOMP error:', frame);
      };

      this.socketClient.activate(); // démarre la connexion
    }
  }
  private handleNotification(notification: Notification) {
    if(!notification)return;
    if(this.selectedChat && this.selectedChat.chatId === notification.chatId){
      switch (notification.notificationType) {
        case 'MESSAGE':
          case 'IMAGE':
            const message: MessageResponse = {
              senderId: notification.senderId,
              receiverId: notification.receiverId,
              content: notification.content,
              type: notification.messageType,
              media: notification.media,
              createdAt: new Date().toString()
            };
            if(notification.notificationType === 'IMAGE'){
                this.selectedChat.lastMessage = 'Attachment';
            }else{
              this.selectedChat.lastMessage = notification.content;
            }

            this.chatMessages.push(message);
            break;
        case 'SEEN':
          this.chatMessages.forEach(m => m.state = 'SEEN');
          break;

      }
    }else{
      const destChat= this.chats.find(c => c.chatId === notification.chatId);
      if(destChat && notification.notificationType!== 'SEEN'){
        if(notification.notificationType === 'MESSAGE'){
          destChat.lastMessage = notification.content;

        }else if(notification.notificationType === 'IMAGE'){
          destChat.lastMessage = 'Attachment';
        }
        destChat.lastMessageTime = new Date().toString();
        destChat.unreadCount! ++;
      }else if(notification.notificationType === 'MESSAGE'){
        const newChat: ChatResponse = {
          chatId:notification.chatId,
          senderId: notification.senderId,
          receiverId: notification.receiverId,
          lastMessage: notification.content,
          name: notification.chatName,
          unreadCount: 1,
          lastMessageTime: new Date().toString()
        }
        this.chats.unshift(newChat);
      }
    }

  }

  ngOnDestroy(): void {
    if(this.socketClient !== null){
      this.socketClient.disconnect();
      this.notificationSubscription.unsubscribe();
      this.socketClient = null;

    }

  }

  private extractFileFromTarget(target: EventTarget | null) :File | null {
    const htmlInputTarget = target as HTMLInputElement;
    if(target === null || htmlInputTarget.files === null || htmlInputTarget.files.length === 0 ){
      return null;
    }

    return htmlInputTarget.files[0];

  }

  ngAfterViewChecked(): void {
    this.scrollBottom();
  }

  private scrollBottom() {
    if(this.scrollableDiv){
      const div = this.scrollableDiv.nativeElement;
      div.scrollTop = div.scrollHeight;
    }

  }
}
