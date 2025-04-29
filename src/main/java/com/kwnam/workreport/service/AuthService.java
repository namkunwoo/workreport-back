package com.kwnam.workreport.service;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.kwnam.workreport.dto.AuthRequest;
import com.kwnam.workreport.dto.RegisterRequest;
import com.kwnam.workreport.entity.User;
import com.kwnam.workreport.repository.UserRepository;
import com.kwnam.workreport.security.JwtUtil;
import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // ✅ 이메일 인증 여부 확인 (추가된 부분)
    public boolean isEmailVerified(String email, String code) {
        return emailService.verifyCode(email, code);
    }

    // ✅ 회원가입 (이메일 인증 필수)
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // 2. 이메일 인증 여부 확인
        if (!emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new RuntimeException("Email verification required.");
        }

        // 3. 사용자 생성 및 저장
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화 저장
            .name(request.getName())
            .email(request.getEmail())
            .build();

        userRepository.save(user);
    }

    // ✅ 로그인 (JWT 토큰 발급)
    public String authenticate(AuthRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOptional.get();

        // 🔐 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        System.out.println("✅ JWT Token 생성: " + token); // 로그 추가
        
        return token;
    }
    
    // ✅ 로그인 연장 시 새 JWT 생성
    public String refreshJwtToken(String oldToken) {
        String username = jwtUtil.getUsernameIfValid(oldToken);
        return jwtUtil.generateToken(username); // 새 JWT 생성 후 반환
    }
}
