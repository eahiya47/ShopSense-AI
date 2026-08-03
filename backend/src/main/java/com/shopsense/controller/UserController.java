package com.shopsense.controller;

import com.shopsense.dto.UserProfileResponse;
import com.shopsense.security.UserPrincipal;
import com.shopsense.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves profile details for the authenticated user.")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserProfileResponse userProfile = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(userProfile);
    }
}
