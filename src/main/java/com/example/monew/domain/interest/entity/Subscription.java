package com.example.monew.domain.interest.entity;


import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.base.BaseEntity;
import com.example.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener.class)
public class Subscription extends BaseCreatableEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Interest interest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
