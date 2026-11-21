package com.miniportal.template;

import com.miniportal.common.InvalidFieldValueException;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateValueValidatorTest {

    private final TemplateValueValidator validator = new TemplateValueValidator();

    private static Template flyer() {
        Template template = new Template("Flyer Frühling", TemplateType.FLYER, 12, 50);
        template.addField(new TemplateField("claim", "Marken-Claim", FieldType.FIXED, "Gemeinsam mehr erreichen.", null, null, 1));
        template.addField(new TemplateField("companyName", "Firmenname", FieldType.PARTNER_DATA, null, "companyName", null, 2));
        template.addField(new TemplateField("headline", "Überschrift", FieldType.EDITABLE, "Jetzt vorbeikommen!", null, 40, 3));
        return template;
    }

    @Test
    void ak4_rejectsValueForFixedField() {
        assertThatThrownBy(() -> validator.validate(flyer(), Map.of("claim", "Anderer Claim")))
                .isInstanceOf(InvalidFieldValueException.class)
                .hasMessage("Feld 'claim' ist nicht änderbar.");
    }

    @Test
    void ak4_rejectsValueForPartnerDataField() {
        assertThatThrownBy(() -> validator.validate(flyer(), Map.of("companyName", "Andere Firma")))
                .isInstanceOf(InvalidFieldValueException.class)
                .hasMessage("Feld 'companyName' ist nicht änderbar.");
    }

    @Test
    void ak4_rejectsUnknownKey() {
        assertThatThrownBy(() -> validator.validate(flyer(), Map.of("foo", "bar")))
                .isInstanceOf(InvalidFieldValueException.class)
                .hasMessageStartingWith("Feld 'foo' gibt es in Vorlage");
    }

    @Test
    void ak5_rejectsValueLongerThanMaxLength() {
        assertThatThrownBy(() -> validator.validate(flyer(), Map.of("headline", "x".repeat(41))))
                .isInstanceOf(InvalidFieldValueException.class)
                .hasMessage("Feld 'headline' darf höchstens 40 Zeichen lang sein.");
    }

    @Test
    void ak5_acceptsValueWithExactlyMaxLength() {
        assertThatCode(() -> validator.validate(flyer(), Map.of("headline", "x".repeat(40))))
                .doesNotThrowAnyException();
    }

    @Test
    void ak3_acceptsEmptyString() {
        assertThatCode(() -> validator.validate(flyer(), Map.of("headline", "")))
                .doesNotThrowAnyException();
    }
}
