package com.miniportal.template.dto;

import com.miniportal.template.FieldType;
import com.miniportal.template.TemplateField;

public record TemplateFieldResponse(
        String key,
        String label,
        FieldType fieldType,
        String defaultValue,
        String partnerAttribute,
        Integer maxLength,
        int position) {

    public static TemplateFieldResponse from(TemplateField field) {
        return new TemplateFieldResponse(
                field.getKey(),
                field.getLabel(),
                field.getFieldType(),
                field.getDefaultValue(),
                field.getPartnerAttribute(),
                field.getMaxLength(),
                field.getPosition());
    }
}
