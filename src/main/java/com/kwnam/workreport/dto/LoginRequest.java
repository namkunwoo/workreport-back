package com.kwnam.workreport.dto;

public class LoginRequest {
    private String email;
    private String password;

    // 기본 생성자
    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
