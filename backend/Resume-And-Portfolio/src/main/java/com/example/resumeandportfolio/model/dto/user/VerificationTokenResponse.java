package com.example.resumeandportfolio.model.dto.user;

/**
 * Email Verification Token Response DTO
 *
 * @author Ji-Seungwoo
 * @version 1.0
 */

public record VerificationTokenResponse(
    String email,
    boolean valid
) {}