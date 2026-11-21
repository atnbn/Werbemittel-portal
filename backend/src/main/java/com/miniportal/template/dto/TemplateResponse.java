package com.miniportal.template.dto;

import com.miniportal.template.Template;
import com.miniportal.template.TemplateType;
import java.util.List;

public record TemplateResponse(
        Long id,
        String name,
        TemplateType type,
        long unitPriceCents,
        int subsidyPercent,
        List<TemplateFieldResponse> fields) {

    public static TemplateResponse from(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getType(),
                template.getUnitPriceCents(),
                template.getSubsidyPercent(),
                template.getFields().stream().map(TemplateFieldResponse::from).toList());
    }
}
