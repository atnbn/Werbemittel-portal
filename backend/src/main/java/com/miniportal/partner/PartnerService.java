package com.miniportal.partner;

import com.miniportal.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
                .orElseThrow(() -> notFound(partnerId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Partner getForUpdate(Long partnerId) {
        return partnerRepository.findByIdForUpdate(partnerId)
                .orElseThrow(() -> notFound(partnerId));
    }

    private static NotFoundException notFound(Long partnerId) {
        return new NotFoundException("Partner " + partnerId + " nicht gefunden.");
    }
}
