package sh.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sh.querydsl.entity.Member;

import java.util.List;

//내가 만든 것을 JPARepository외에 추가로 상속받게 해줘야 사용할 수 있다.
public interface MemberRepository extends JpaRepository<Member,Long>,MemberRepositoryCustom {

    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
