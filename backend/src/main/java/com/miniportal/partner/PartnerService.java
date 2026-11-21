package com.miniportal.partner;

import com.miniportal.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @Transactional(readOnly = true)
    public Partner getOwnPartner(Long partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new NotFoundException("Partner " + partnerId + " nicht gefunden."));
    }
}
