package com.miniportal.security;

import com.miniportal.security.dto.MeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public MeResponse getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        return MeResponse.from(user);
    }
}
