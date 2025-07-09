package park.pharmatc.v1.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import park.pharmatc.v1.dto.auth.LoginRequest;
import park.pharmatc.v1.dto.auth.SignupRequest;
import park.pharmatc.v1.entity.User;
import park.pharmatc.v1.repository.UserRepository;
import park.pharmatc.v1.security.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider; // ✅ 추가

    public void signup(SignupRequest request) {

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 객체 생성
        User user = new User();
        user.setPharmacyName(request.getPharmacyName());
        user.setOwnerName(request.getOwnerName());
        user.setBirthDate(request.getBirthDate());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setEmailVerified(false);  // 이메일 인증 아직 안됨

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.generateToken(user.getEmail());
    }

}
