package com.kwnam.workreport.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kwnam.workreport.dto.AuthRequest;
import com.kwnam.workreport.dto.RegisterRequest;
import com.kwnam.workreport.entity.User;
import com.kwnam.workreport.repository.UserRepository;
import com.kwnam.workreport.security.JwtUtil;
import com.kwnam.workreport.service.AuthService;
import com.kwnam.workreport.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, EmailService emailService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }
    
 // ✅ 테스트용 API (JWT 필요)
    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(Map.of("message", "Hello from secured API"));
    }


    // ✅ 회원가입 API (이메일 인증 필수)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 🔥 이메일 인증 확인
            if (!authService.isEmailVerified(request.getEmail(), request.getVerificationCode())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ 이메일 인증이 필요합니다."));
            }

            authService.register(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 회원가입이 완료되었습니다!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ 회원가입에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // ✅ 이메일 중복 체크
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ✅ 로그인 API (JWT + 사용자 정보 반환)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            String token = authService.authenticate(request);
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                    "username", user.getUsername(),
                    "name", user.getName(),
                    "email", user.getEmail()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "❌ 로그인 실패: 아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    // ✅ 이메일 인증 코드 요청 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            emailService.sendVerificationCode(request.get("email"));
            return ResponseEntity.ok("✅ 인증 코드가 이메일로 전송되었습니다.");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("❌ 인증 코드 전송에 실패했습니다.");
        }
    }

    // ✅ 인증 코드 확인 API
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isValid = emailService.verifyCode(email, code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ 인증 코드가 올바르지 않습니다."));
        }
    }

    // ✅ 로그인 상태 유지 API (현재 로그인한 사용자 정보 조회)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
    	System.out.println("✅ /me 진입 ");
        if (token == null || token.isEmpty()) {
        	System.out.println("❌ 토큰이 필요합니다.");
            return ResponseEntity.badRequest().body(Map.of("message", "❌ 토큰이 필요합니다."));
            
        }

        try {
        	System.out.println("✅ /me try catch 문 진입 ");
            String username = jwtUtil.getUsernameIfValid(token.replace("Bearer ", ""));
            Optional<User> user = userRepository.findByUsername(username);

            System.out.println("✅ /me findByUsername 진행 ");
            
            if (user.isEmpty()) {
            	System.out.println("❌ 유효하지 않은 토큰입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ 유효하지 않은 토큰입니다."));
            }

            System.out.println("✅ 현재 로그인 사용자: " + user.get().getUsername());
            return ResponseEntity.ok(user.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "❌ 토큰 검증 실패"));
        }
    }
    
    // ✅ 새 JWT 발급 (로그인 연장)
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String newToken = authService.refreshJwtToken(token.replace("Bearer ", ""));
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "토큰 갱신 실패"));
        }
    }

    // ✅ 비밀번호 재설정 이메일 요청
    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            emailService.sendPasswordResetLink(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 비밀번호 재설정 링크가 이메일로 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ 이메일 전송 실패: " + e.getMessage()));
        }
    }

    // ✅ 비밀번호 재설정 완료
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("success", true, "message", "✅ 비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "❌ 비밀번호 변경 실패: " + e.getMessage()));
        }
    }

    
}
