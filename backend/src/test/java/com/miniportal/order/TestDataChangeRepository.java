package com.miniportal.order;

import com.miniportal.partner.Partner;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface TestDataChangeRepository extends Repository<Partner, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Partner p set p.companyName = :companyName where p.id = :partnerId")
    int changeCompanyName(Long partnerId, String companyName);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TemplateField f set f.defaultValue = :defaultValue where f.template.id = :templateId and f.key = :key")
    int changeDefaultValue(Long templateId, String key, String defaultValue);
}
