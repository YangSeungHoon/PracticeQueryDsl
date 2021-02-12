package sh.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sh.querydsl.dto.MemberDto;
import sh.querydsl.dto.QMemberDto;
import sh.querydsl.dto.UserDto;
import sh.querydsl.entity.Member;
import sh.querydsl.entity.QMember;
import sh.querydsl.entity.QTeam;
import sh.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static sh.querydsl.entity.QMember.*;
import static sh.querydsl.entity.QTeam.*;

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

    @Test
    public void aggregation() throws Exception {

        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    //팀의 이름과 각 팀의 평균 연령 구하기
    @Test
    public void group() throws Exception {

        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    //팀 A에 소속된 모든 회원
    @Test
    public void join() throws Exception {

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

  assertThat(result)
          .extracting("username")
          .containsExactly("member1","member2");

    }

    //세타 조인
    //회원의 이름이 팀 이름과 같은 회원 조회
    @Test
    public void theta_join() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }


    // 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    //JPQL => select m, t from Member m left join m.team on t.name = 'teamA'
    @Test
    public void join_on_filtering() throws Exception {

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA")) //member 기준으로 join
                .fetch();

        //위에서 leftjoin이 아닌, 그냥 join을 쓴다고 가정하면 아래와 결과가 같다
        List<Tuple> result2 = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team,team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // 연관관계가 없는 엔티티 외부 조인
    // 회원의 이름이 팀 이름과 같은 대상 외부 조인
    @Test
    public void join_on_no_relation() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member,team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                //보통은 이렇게 조인을 하는데, 이렇게하면 join on 절에 id값이 들어간다. 그래서 조인하는 대상을 id로 매칭을 한다.
                //그러나 위와 같이하면 id로 매칭을하지 않아서 단순히 member.username에해당하는 team.name으로만 조인을 한다.
                //leftjoin(member.team, team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {

        em.flush();
        em.clear();

        //Member Entity에서 Team에 해당하는 속성이 Lazy타입으로 되어있어서 그 team을 쓰기전까지, 즉 아래의 sql문으로는
        //team을 가져오지않는다.
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() throws Exception {

        em.flush();
        em.clear();


        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isTrue();
    }

    @Test
    public void basicCase() throws Exception {

        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() throws Exception {

        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    //나이가 가장 많은 회원 조회
    @Test
    public void subQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        System.out.println("result = " + result);

        assertThat(result).extracting("age")
                .containsExactly(40);

    }

    //나이가 평균 이상인 회원
    @Test
    public void subQueryGoe() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        System.out.println("result = " + result);

        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    //In 사용
    @Test
    public void subQueryIn() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age.max())
                                .from(memberSub)
                        .where(memberSub.age.gt(10)) //10살 초과인 것.
                ))
                .fetch();

        System.out.println("result = " + result);

        assertThat(result).extracting("age")
                .containsExactly(20,30,40);
    }

    //select절에 subquery
    //나이가 가장 많은 회원 조회
    @Test
    public void selectSubQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory//JPAExpressions
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();


        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    //from 절의 서브쿼리 한계
    // JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 QueryDsl도 지원하지 않는다.
    // 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. 그래서 Querydsl도 select절의 서브쿼리는 지원한다.

    //from 절의 서브쿼리 해결방안
    // 서브쿼리를 join으로 변경한다.(가능한 상황도 있고, 불가능한 상황도 있음.)
    // 애플리케이션에서 쿼리를 2번 분리해서 사용한다
    // nativeSQL을 사용한다.


    //상수 사용하기 Expressions.constant
    @Test
    public void constant() throws Exception {

        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    //문자 더하기 concat,StringValue(enum타입에도 사용할 수 있다.)
    @Test
    public void concat() throws Exception {

        //{username}_{age}
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void simpleProjection() throws Exception {

        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() throws Exception {

        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("age = " + age);
            System.out.println("username = " + username);
        }
    }

    //dto로 뽑아내는데 JPQL을 사용하는 경우에는 new 키워드로 생성자로 받아야한다.
    @Test
    public void findDtoByJPQL() throws Exception {

        List<MemberDto> result = em.createQuery("select new sh.querydsl.dto.MemberDto(m.username,m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //Setter 이용하기(이름 매칭이 중요함)
    @Test
    public void findDtoBySetter() throws Exception {

        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //field로 넣어주기(이름 매칭이 중요함)
    @Test
    public void findDtoByField() throws Exception {

        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //Constructor이용하기(타입이 중요함)
    @Test
    public void findDtoByConstructor() throws Exception {

        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }



    //별칭 사용하기
    @Test
    public void findUserDto() throws Exception {

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    //select절 서브쿼리 별칭 사용하기
    @Test
    public void findUserDtoSubAs() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                    .from(memberSub),"age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    //어노테이션을 붙이고, Q파일을 생성해서 해결하는 방법.
    //단점으로는 dto가 QueryDsl에 의존하게되는 것이 있다.
    @Test
    public void findDtoByQueryProjection() throws Exception {

        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //동적 쿼리를 해결하는 방식
    //- BooleanBuilder 방식
    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception {

        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        //member의 username이 필수라면 이렇게 초기코드를 넣어줄 수도 있다.
        //BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond));
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                    .selectFrom(member)
                    .where(builder)
                    .fetch();
    }


    // Where 다중 파라미터 사용
    @Test
    public void dynamicQuery_WhereParam() throws Exception {

        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond),ageEq(ageCond)) //where 에 null값이 들어가면 그냥 무시한다.
                //.where(allEq(usernameCond,ageCond)) //조립한거..
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {

        if (usernameCond == null) {
            return null;
        }
        return member.username.eq(usernameCond);
    }

    private BooleanExpression ageEq(Integer ageCond){

        if (ageCond == null) {
            return null;
        }
        return member.age.eq(ageCond);
    }

    //이런식으로 위에 두 개를 합쳐서 조립도 가능하다.
    private BooleanExpression allEq(String usernameCond,Integer ageCond) {

        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    //bulk연산은 영속성 컨텍스트를 거치지 않고 바로 DB로 쿼리를 날리기 때문에
    //em.flush, em.clear를 통해 영속성 컨텍스트를 DB와 맞춰주고, 그 이후에 값을 가져오게 해야한다.
    // 물론 bulk연산을 하면 DB의 값은 update가 되어있다. 다만 영속성 컨텍스트의 값이 안바뀌어 있는 것이 문제.
    @Test
    public void bulkUpdate() throws Exception {

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))//28살 미만이면
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .select(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void bulkAdd() throws Exception {

        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))// 더하기 => add // 곱하기 => multiplay
                .execute();
    }

    @Test
    public void bulkDelete() throws Exception {

        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18)) //18살 이상
                .execute();

    }


}
