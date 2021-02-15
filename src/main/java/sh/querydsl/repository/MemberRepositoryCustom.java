package sh.querydsl.repository;

import sh.querydsl.dto.MemberSearchCondition;
import sh.querydsl.dto.MemberTeamDto;

import java.util.List;

//SpringData JPARepository를 사용하면서 내가 직접 구현하여 사용하고 싶은 경우..
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);
}
