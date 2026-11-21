package com.miniportal.template;

import com.miniportal.common.InvalidFieldValueException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateValueValidator {

    public void validate(Template template, Map<String, String> values) {
        Map<String, TemplateField> fieldsByKey = template.getFields().stream()
                .collect(Collectors.toMap(TemplateField::getKey, Function.identity()));
        values.forEach((key, value) -> validateValue(template, fieldsByKey.get(key), key, value));
    }

    private static void validateValue(Template template, TemplateField field, String key, String value) {
        if (field == null) {
            throw new InvalidFieldValueException(
                    "Feld '" + key + "' gibt es in Vorlage " + template.getId() + " nicht.");
        }
        if (field.getFieldType() != FieldType.EDITABLE) {
            throw new InvalidFieldValueException("Feld '" + key + "' ist nicht änderbar.");
        }
        if (field.getMaxLength() != null && value.length() > field.getMaxLength()) {
            throw new InvalidFieldValueException(
                    "Feld '" + key + "' darf höchstens " + field.getMaxLength() + " Zeichen lang sein.");
        }
    }
}
