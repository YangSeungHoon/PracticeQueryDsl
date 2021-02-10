package sh.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sh.querydsl.entity.Member;
import sh.querydsl.entity.QMember;
import sh.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static sh.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QueryBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }

    @Test
    public void startJPQL() throws Exception {

        String qlString =
                "select m from Member m " +
                        "where m.username = :username";

        Member findMember = em.createQuery(qlString,Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() throws Exception {


        //기본 인스턴스를 static import로 작성
        QMember m = member;
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        //        //인스턴스로
//        QMember m = QMember.member;
//        Member findMember = queryFactory
//                .select(QMember.member)
//                .from(QMember.member)
//                .where(QMember.member.username.eq("member1"))
//                .fetchOne();



        //별칭 직접 지정
        //QMember m = new QMember("m");

//        Member findMember = queryFactory
//                .select(m)
//                .from(m)
//                .where(m.username.eq("member1"))
//                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() throws Exception {

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    //and의 또 다른 방법
    @Test
    public void searchAndParam() throws Exception {

        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                        )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() throws Exception {

        //리스트 조회, 데이터 없으면 빈 리스트 반환
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        //단 건 조회, 결과가 없으면 null, 둘 이상이면 NonUniqueResultException
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        //limit(1).fetchOne()
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        //페이징 정보 포함, total count쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();

        //member의 갯수만 가져옴.
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }


    //회원 정렬 순서
    //1. 회원 나이 내림차순(desc)
    //2. 회원 이름 오름차순(asc)
    //3.단 2에서 회원 이름이 없으면 마지막에 출력(nulls last) => null first도 있음.
    @Test
    public void sort() throws Exception {

        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() throws Exception {

        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc()) //유저 이름 기준 내림차순
                .offset(1) //0부터 시작이니까 앞에 1개 스킵하고 1부터
                .limit(2) //2 까지
                .fetch();

        assertThat(result.size()).isEqualTo(2); //1,2 두개.
    }

    @Test
    public void paging2() throws Exception {

        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc()) //유저 이름 기준 내림차순
                .offset(1) //0부터 시작이니까 앞에 1개 스킵하고 1부터
                .limit(2) //2 까지
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }


}
