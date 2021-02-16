package sh.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import sh.querydsl.entity.Member;

import java.util.List;

//내가 만든 것을 JPARepository외에 추가로 상속받게 해줘야 사용할 수 있다.
//extends로 QuerydlsPredicaeExecutor를 하면 여기 들어있는 기능들을 다 쓸 수 있다.
public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom , QuerydslPredicateExecutor<Member> {

    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
