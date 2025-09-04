package com.kwnam.workreport.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.kwnam.workreport.entity.PasswordResetToken;
import com.kwnam.workreport.entity.User;
import com.kwnam.workreport.repository.PasswordResetTokenRepository;
import com.kwnam.workreport.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>(); // ✅ HashMap → ConcurrentHashMap 변경
    private final Map<String, Long> verificationExpiry = new ConcurrentHashMap<>(); // 인증 코드 만료 시간 저장
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    public EmailService(JavaMailSender mailSender, UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    // ✅ 랜덤 6자리 인증 코드 생성
    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000)); // 100000 ~ 999999
    }

    // ✅ 이메일 전송 및 코드 저장 (로그 추가)
    public void sendVerificationCode(String email) throws MessagingException {
        String code = generateVerificationCode();
        verificationCodes.put(email, code); // ✅ 인증 코드 저장
        verificationExpiry.put(email, System.currentTimeMillis() + 5 * 60 * 1000); // 5분 후 만료
        System.out.println("📩 인증 코드 저장: " + email + " → " + code); // 로그 추가

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Your Email Verification Code");
        helper.setText("Your verification code is: " + code);

        mailSender.send(message);
    }

    // ✅ 인증 코드 검증 (로그 추가)
    public boolean verifyCode(String email, String code) {
    	Long expiryTime = verificationExpiry.get(email);
        if (expiryTime == null || System.currentTimeMillis() > expiryTime) {
            verificationCodes.remove(email);
            verificationExpiry.remove(email);
            return false; // 만료된 코드
        }
    	
        boolean isValid = verificationCodes.containsKey(email) && verificationCodes.get(email).equals(code);
        System.out.println("🔍 인증 코드 검증: " + email + " → 입력된 코드: " + code + " | 저장된 코드: " + verificationCodes.get(email));
        return isValid;
    }
    
    public void sendPasswordResetLink(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("해당 이메일의 사용자가 없습니다.");
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(userOpt.get())
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();

        passwordResetTokenRepository.save(resetToken);

        String link = "http://localhost:3000/reset-password?token=" + token;
        String messageBody = "아래 링크를 클릭하여 비밀번호를 재설정하세요:\n" + link;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("비밀번호 재설정 링크");
        helper.setText(messageBody);

        mailSender.send(message);
    }

}
