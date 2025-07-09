package park.pharmatc.v1.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    private String pharmacyName;    // 약국명
    private String ownerName;       // 대표자 이름
    private String birthDate;       // 생년월일 ("YYMMDD")
    private String email;           // 로그인 ID 및 인증용
    private String password;        // 비밀번호

}
