package park.pharmatc.v1.service;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value; // ✅ 이걸로 수정!
import park.pharmatc.v1.entity.User;
import park.pharmatc.v1.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository; // ✅ 추가


    // 인증코드를 메모리에 잠깐 저장 (실제 운영은 Redis 권장)
    private final Map<String, String> verificationCodes = new HashMap<>();

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail) {
        String code = generateCode();
        verificationCodes.put(toEmail, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[pharmATC] 이메일 인증 코드");
        message.setText("인증코드는 다음과 같습니다: " + code);
        message.setFrom(fromEmail);

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        String saved = verificationCodes.get(email);
        if (saved != null && saved.equals(code)) {
            // ✅ 인증 성공 → DB 업데이트
            Optional<User> optionalUser = userRepository.findByEmail(email);
            optionalUser.ifPresent(user -> {
                user.setEmailVerified(true);
                userRepository.save(user);
            });

            // ✅ 인증코드 제거 (선택)
            verificationCodes.remove(email);

            return true;
        }
        return false;
    }


    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
