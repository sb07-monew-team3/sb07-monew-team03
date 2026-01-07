package com.example.monew.domain.user.mapper;

import com.example.monew.domain.user.dto.UserDto;
import com.example.monew.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getNickName(),
                user.getCreatedAt()
        );


    }
}
