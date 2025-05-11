import {Component, EventEmitter, inject, input, InputSignal, Output, output} from '@angular/core';
import {ChatResponse} from '../../services/models/chat-response';
import {DatePipe} from '@angular/common';
import {UserResponse} from '../../services/models/user-response';
import {ChatControllerService, UserControllerService} from '../../services/services';
import {KeycloakService} from '../../utils/keycloak/keycloak.service';

@Component({
  selector: 'app-chat-list',
  imports: [
    DatePipe
  ],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent {
  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  contacts: Array<UserResponse> = [];
  chatSelected = output<ChatResponse>();
  searchNewContact:boolean = false;
  userService = inject(UserControllerService);
  chatService = inject(ChatControllerService);
  keycloakService = inject(KeycloakService);





  searchContact() {
    this.userService.getAllUsers()
      .subscribe({
      next: (users) => {
        this.contacts = users;
        this.searchNewContact = true;
      },
      error: (err) => {
        console.log(err);
      }
    })

  }

  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);

  }


  wrapMessage(message: string | undefined):string {
    // @ts-ignore
    if(message?.trim().length && message.trim().length <= 20){
      return <string>message;
    }
    return  message?.substring(0, 17) + '...';
  }

  selectContact(contact: UserResponse) {
    this.chatService.createChat({
      'sender-id': this.keycloakService.userId as string,
      'receiver-id': contact.userId as string
    }).subscribe({
      next: (res) => {
        const chat:ChatResponse = {
          chatId: res.response,
          name: contact.firstName + ' ' + contact.lastName,
          receiverOnline: contact.online,
          lastMessageTime: contact.lastSeen,
          senderId: this.keycloakService.userId,
          receiverId: contact.userId,
        };
        this.chats().unshift(chat);
        this.searchNewContact = false;
        this.chatSelected.emit(chat);
      }
    })

  }
}
