package com.example.monew.domain.article.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "articles")
@EntityListeners(AuditingEntityListener.class)
public class Article extends BaseCreatableEntity {

    @Column(name="source", nullable = false, updatable = false)
    private String source;

    @Column(name="source_url", nullable = false, updatable = false, unique = true)
    private String sourceUrl;

    @Column(name="title", nullable = false, updatable = false)
    private String title;

    @Column(name="publish_date", nullable = false, updatable = false)
    private LocalDateTime publishDate;

    @Column(name="summary", updatable = false)
    private String summary;

    @Column(name="is_deleted", nullable = false)
    @ColumnDefault( "false")
    private boolean isDeleted;

    @OneToMany
    @JoinTable(
            name = "articles_interests",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private List<Interest> interests;
}
