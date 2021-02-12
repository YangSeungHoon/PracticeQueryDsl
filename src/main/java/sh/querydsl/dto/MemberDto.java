package sh.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection //이거 있으면 dto도 Q파일로 생성한다.
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
