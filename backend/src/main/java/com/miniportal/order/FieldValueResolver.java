package com.miniportal.order;

import com.miniportal.template.TemplateField;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FieldValueResolver {

    public Map<String, String> resolve(List<TemplateField> fields, Map<String, String> values,
                                       Function<String, String> partnerAttributes) {
        Map<String, String> resolved = new LinkedHashMap<>();
        fields.stream()
                .sorted(Comparator.comparingInt(TemplateField::getPosition))
                .forEach(field -> resolved.put(field.getKey(), resolveValue(field, values, partnerAttributes)));
        return resolved;
    }

    private static String resolveValue(TemplateField field, Map<String, String> values,
                                       Function<String, String> partnerAttributes) {
        return switch (field.getFieldType()) {
            case FIXED -> field.getDefaultValue();
            case PARTNER_DATA -> partnerAttributes.apply(field.getPartnerAttribute());
            case EDITABLE -> values.getOrDefault(field.getKey(), field.getDefaultValue());
        };
    }
}
