package com.kwnam.workreport.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>(); // ✅ HashMap → ConcurrentHashMap 변경
    private final Map<String, Long> verificationExpiry = new ConcurrentHashMap<>(); // 인증 코드 만료 시간 저장

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
}
