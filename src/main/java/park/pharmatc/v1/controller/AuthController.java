package park.pharmatc.v1.controller;

//회원가입 인증 이메일 컨트롤러
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.dto.auth.LoginRequest;
import park.pharmatc.v1.dto.auth.SignupRequest;
import park.pharmatc.v1.service.AuthService;
import park.pharmatc.v1.service.EmailService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    // ✅ 1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 완료");
    }

    // ✅ 2. 이메일 인증코드 요청
    @PostMapping("/email-code")
    public ResponseEntity<?> sendEmailCode(@RequestParam String email) {
        emailService.sendVerificationCode(email);
        return ResponseEntity.ok("인증코드가 이메일로 발송되었습니다.");
    }

    // ✅ 3. 이메일 인증번호 검증
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailService.verifyCode(email, code);
        if (verified) {
            return ResponseEntity.ok("이메일 인증 완료");
        } else {
            return ResponseEntity.badRequest().body("인증 실패: 코드가 일치하지 않습니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok().body("Bearer " + token);
    }

}