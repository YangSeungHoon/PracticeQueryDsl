package sh.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sh.querydsl.dto.MemberSearchCondition;
import sh.querydsl.dto.MemberTeamDto;

import java.util.List;

//SpringData JPARepository를 사용하면서 내가 직접 구현하여 사용하고 싶은 경우..
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,Pageable pageable);
    Page<MemberTeamDto> searchPageUpgrade(MemberSearchCondition condition,Pageable pageable);
}
