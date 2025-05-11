package com.thm.whatsappclone.services;


import com.thm.whatsappclone.mapper.UserMapper;
import com.thm.whatsappclone.repository.UserRepository;
import com.thm.whatsappclone.response.UserResponse;
import com.thm.whatsappclone.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public List<UserResponse> getAllUsersExceptSelf(Authentication connectedUser){
        return userRepository.findAllUsersExceptSelf(connectedUser.getName())
                .stream()
                .map(userMapper::toUserResponse)
                .toList();


    }
}
