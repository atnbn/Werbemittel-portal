package com.miniportal.partner.dto;

import com.miniportal.partner.Partner;

public record PartnerResponse(
        Long id,
        String companyName,
        String street,
        String zip,
        String city,
        String phone,
        long balanceCents) {

    public static PartnerResponse from(Partner partner) {
        return new PartnerResponse(
                partner.getId(),
                partner.getCompanyName(),
                partner.getStreet(),
                partner.getZip(),
                partner.getCity(),
                partner.getPhone(),
                partner.getBalanceCents());
    }
}
