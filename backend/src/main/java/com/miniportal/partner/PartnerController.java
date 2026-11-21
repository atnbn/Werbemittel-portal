package com.miniportal.partner;

import com.miniportal.partner.dto.PartnerResponse;
import com.miniportal.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping("/me")
    public PartnerResponse getOwnPartner(@AuthenticationPrincipal AuthenticatedUser user) {
        return PartnerResponse.from(partnerService.getOwnPartner(user.getPartnerId()));
    }
}
