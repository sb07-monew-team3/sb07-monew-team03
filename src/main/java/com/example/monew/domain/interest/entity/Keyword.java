package com.example.monew.domain.interest.entity;

import com.example.monew.domain.base.BaseEntity;
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
@Table(
        name = "keywords",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_keywords_interest_keyword",
                        columnNames = {"interest_id", "keyword"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Keyword extends BaseEntity {

    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Interest interest;



}
