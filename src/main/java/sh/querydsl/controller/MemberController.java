package sh.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sh.querydsl.dto.MemberSearchCondition;
import sh.querydsl.dto.MemberTeamDto;
import sh.querydsl.repository.MemberJpaRepository;
import sh.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }


//http://localhost:8080/v2/members?ageGoe=99&page=0&size=50
    //전체 데이터는 100개 넣어놓은 상태인데 페이징 사이즈를 50개로 했다.그리고 조건에 99살 이사응로 조건을 걸었기 때문에 조건에 부합하는 값은 많아야
    //5개를 넘지 못한다. 그렇기 때문에 위의 경우, count쿼리를 날리지 않는다.
    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageUpgrade(condition,pageable);
    }

    //http://localhost:8080/v3/members?page=0&size=5
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageUpgrade(condition,pageable);
    }

}
