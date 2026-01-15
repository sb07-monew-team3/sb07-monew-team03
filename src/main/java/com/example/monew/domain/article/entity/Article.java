package com.example.monew.domain.article.entity;

import com.example.monew.domain.base.BaseCreatableEntity;
import com.example.monew.domain.interest.entity.Interest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "articles")
@EntityListeners(AuditingEntityListener.class)
public class Article extends BaseCreatableEntity {

    @Column(name="source", nullable = false, updatable = false)
    private String source;

    @Column(name="source_url", nullable = false, updatable = false, unique = true, length = 500)
    private String sourceUrl;

    @Column(name="title", nullable = false, updatable = false)
    private String title;

    @Column(name="publish_date", nullable = false, updatable = false)
    private LocalDateTime publishDate;

    @Column(name="summary", updatable = false)
    private String summary;

    @Column(name="is_deleted", nullable = false)
    @ColumnDefault( "false")
    @JsonProperty("deleted") // json 직렬화시 deleted로 저장, 역직렬화 매핑을 위해 어노테이션 추가
    private boolean isDeleted;

    @Column(name="sort_timestamp", nullable = false, updatable = false, unique = true)
    private Instant sortTimestamp;

    public void setSortTimestamp(Instant sortTimestamp) {
        this.sortTimestamp = sortTimestamp;
    }

    public void updateInterests(List<Interest> interests) {
        if(interests == null || interests.isEmpty()) return;

        for (Interest interest : interests) {
            if(!this.interests.contains(interest)) {
                this.interests.add(interest);
            }
        }
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // 백업 시에는 id까지 저장, 복구 시에는 id를 가져오지 않는다.(물리 삭제 데이터 복구 위함)
    public UUID getId() {
        return super.getId();
    }

    @OneToMany
    @JoinTable(
            name = "articles_interests",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private List<Interest> interests;

    public void deleteLogic() {
        this.isDeleted = true;
    }

    public void restoreLogic() {
        this.isDeleted = false;
    }
}
