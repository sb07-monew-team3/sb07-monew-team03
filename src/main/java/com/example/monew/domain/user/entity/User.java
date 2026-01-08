package com.example.monew.domain.user.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Table(name = "users")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseCreatableEntity {

    @Column(name="email",nullable = false,unique = true,updatable = false,length = 100)
    private String email;

    @Column(name="nickname",nullable = false,length = 100)
    private String nickName;

    @Column(name="password",nullable = false,updatable = false,length = 100)
    private String password;

    @Column(name="deleted_at")
    private Instant deletedAt;

    public void deleteLogic(){
        this.deletedAt = Instant.now();
    }

    public void updateNickName(String nickName){
        this.nickName = nickName;
    }
}
