package com.example.monew.domain.interest.unit.repository;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.global.config.JpaAuditingConfig;
import com.example.monew.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
public class InterestRepositoryTest {

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp(){
        Interest interest = new Interest("코딩");
        Interest interest2 = new Interest("하드웨어");
        Interest interest3 = new Interest("금융");

        interestRepository.saveAll(List.of(interest, interest2, interest3));

        List<Keyword> keywords = List.of(
                new Keyword("자바", interest),
                new Keyword("스프링", interest),
                new Keyword("그래픽카드", interest2),
                new Keyword("CPU", interest2),
                new Keyword("비트코인", interest3),
                new Keyword("코스닥", interest3)
        );

        keywordRepository.saveAll(keywords);

        em.flush();
        em.clear();
    }
    
    @Test
    @DisplayName("관심사 이름으로 검색할 수 있다")
    void search_interestName_Success() {

        // given
        String keyword = "코딩";
        String orderBy = "name";
        String direction = "asc";
        String cursor = null;
        Instant after = null;
        int limit = 10;

        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(search).hasSize(1);
        assertThat(search.get(0).getName()).isEqualTo("코딩");
    }

    @Test
    @DisplayName("키워드 이름으로 검색할 수 있다")
    void search_keywordName_Success () {

        // given
        String keyword = "자바";
        String orderBy = "name";
        String direction = "asc";
        String cursor = null;
        Instant after = null;
        int limit = 10;

        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(search).hasSize(1);
        assertThat(search.get(0).getName()).isEqualTo("코딩");
    }




}
