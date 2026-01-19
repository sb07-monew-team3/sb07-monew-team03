package com.example.monew.domain.interest.unit.repository;

import com.example.monew.domain.interest.entity.Interest;
import com.example.monew.domain.interest.entity.Keyword;
import com.example.monew.domain.interest.entity.Subscription;
import com.example.monew.domain.interest.repository.InterestRepository;
import com.example.monew.domain.interest.repository.KeywordRepository;
import com.example.monew.domain.interest.repository.SubscriptionRepository;
import com.example.monew.domain.user.entity.User;
import com.example.monew.domain.user.repository.UserRepository;
import com.example.monew.global.config.JpaAuditingConfig;
import com.example.monew.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
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
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EntityManager em;

    private Interest coding;
    private Interest hardware;
    private Interest finance;


    @BeforeEach
    void setUp() {

        subscriptionRepository.deleteAll();
        keywordRepository.deleteAll();
        interestRepository.deleteAll();

        em.flush();
        em.clear();

        coding = new Interest("코딩");
        hardware = new Interest("하드웨어");
        finance = new Interest("금융");

        Instant now = Instant.now();
        ReflectionTestUtils.setField(coding, "createdAt", now);
        ReflectionTestUtils.setField(hardware, "createdAt", now.plusMillis(20));
        ReflectionTestUtils.setField(finance, "createdAt", now.plusMillis(40));

        interestRepository.saveAll(List.of(coding, hardware, finance));


        List<Keyword> keywords = List.of(
                new Keyword("자바", coding),
                new Keyword("스프링", coding),
                new Keyword("그래픽카드", hardware),
                new Keyword("CPU", hardware),
                new Keyword("비트코인", finance),
                new Keyword("코스닥", finance)
        );
        keywordRepository.saveAll(keywords);

        User user1 = new User("a@a.com", "테스트1", "1234", null);
        User user2 = new User("b@b.com", "테스트2", "1234", null);
        User user3 = new User("c@c.com", "테스트3", "1234", null);
        userRepository.saveAll(List.of(user1, user2, user3));

        Subscription sub1 = new Subscription(coding, user1);
        Subscription sub2 = new Subscription(coding, user2);
        Subscription sub3 = new Subscription(coding, user3);
        Subscription sub4 = new Subscription(hardware, user1);
        Subscription sub5 = new Subscription(hardware, user2);
        Subscription sub6 = new Subscription(finance, user1);
        subscriptionRepository.saveAll(List.of(sub1, sub2, sub3, sub4, sub5, sub6));

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
    
    @Test
    @DisplayName("이름을 내림차순으로 정렬한다")
    void search_sortByName_desc() {

        // given
        String keyword = null; //전체 조회하기
        String orderBy = "name";
        String direction = "desc";
        String cursor = null;
        Instant after = null;
        int limit = 10;

        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(keyword, orderBy, direction, cursor, after, limit);


        // then
        assertThat(search).hasSize(3);
        assertThat(search).extracting("name")
                .containsExactly("하드웨어", "코딩", "금융");

    }
    
    @Test
    @DisplayName("구독자수 오름차순으로 정렬한다")
    void search_sortBySubscriptionCount_asc() {

        // given
        String keyword = null; //전체 조회하기
        String orderBy = "subscriberCount";
        String direction = "asc";
        String cursor = null;
        Instant after = null;
        int limit = 10;
        
        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(keyword, orderBy, direction, cursor, after, limit);
        
        
        // then
        assertThat(search).hasSize(3);
        assertThat(search).extracting("name")
                .containsExactly("금융", "하드웨어", "코딩");
    }

    @Test
    @DisplayName("이름 오름차순 기준 커서로 동작한다")
    void search_nameCursor_asc() {

        // given
        String keyword = null; //전체 조회하기
        String orderBy = "name";
        String direction = "asc";
        String cursor = "코딩";
        Instant after = Instant.now();
        int limit = 10;

        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(
                keyword, orderBy, direction, cursor, after, limit);

        // then
        assertThat(search).hasSize(1);
        assertThat(search.get(0).getName()).isEqualTo("하드웨어");
        assertThat(search).extracting("name").doesNotContain("코딩", "금융");
    }

    @Test
    @DisplayName("구독자수 내림차순 기준 커서가 동작한다")
    void search_subscriptionCountCursor_desc() {

        // given
        String keyword = null; //전체 조회하기
        String orderBy = "subscriberCount";
        String direction = "desc";
        String cursor = "3";
        Instant after = coding.getCreatedAt();
        int limit = 10;

        // when
        List<Interest> search = interestRepository.searchByInterestOrKeyword(
                keyword, orderBy, direction, cursor, after, limit);


        // then
        assertThat(search).hasSize(2);
        assertThat(search).extracting("name").containsExactly("하드웨어", "금융");
        assertThat(search).extracting("name").doesNotContain("코딩");
    }
}
