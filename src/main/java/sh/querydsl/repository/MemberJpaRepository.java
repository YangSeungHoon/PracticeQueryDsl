package sh.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sh.querydsl.entity.Member;
import sh.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static sh.querydsl.entity.QMember.member;

@Repository
//@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    //이 코드 대신에 위에 @RequiredArgsConstructor 사용 가능.
    public MemberJpaRepository(EntityManager em,JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findByID(Long id) {
        Member findMember = em.find(Member.class,id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m",Member.class)
                .getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }


    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username =: username",Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {

        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
}
