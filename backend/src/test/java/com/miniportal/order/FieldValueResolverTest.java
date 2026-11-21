package com.miniportal.order;

import com.miniportal.template.FieldType;
import com.miniportal.template.TemplateField;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldValueResolverTest {

    private static final List<TemplateField> FIELDS = List.of(
            new TemplateField("claim", "Marken-Claim", FieldType.FIXED, "Gemeinsam mehr erreichen.", null, null, 1),
            new TemplateField("companyName", "Firmenname", FieldType.PARTNER_DATA, null, "companyName", null, 2),
            new TemplateField("phone", "Telefon", FieldType.PARTNER_DATA, null, "phone", null, 3),
            new TemplateField("headline", "Überschrift", FieldType.EDITABLE, "Sommerangebote", null, 30, 4));

    private static final Map<String, String> PARTNER_ATTRIBUTES = Map.of(
            "companyName", "Autohaus Müller",
            "phone", "09531 12345");

    private final FieldValueResolver resolver = new FieldValueResolver();

    @Test
    void ak3_resolvesFixedPartnerDataAndEditableValues() {
        Map<String, String> resolved = resolver.resolve(FIELDS, Map.of("headline", "Sommerfest!"), PARTNER_ATTRIBUTES::get);

        assertThat(resolved).containsExactly(
                Map.entry("claim", "Gemeinsam mehr erreichen."),
                Map.entry("companyName", "Autohaus Müller"),
                Map.entry("phone", "09531 12345"),
                Map.entry("headline", "Sommerfest!"));
    }

    @Test
    void ak3_missingEditableFieldGetsDefaultValue() {
        Map<String, String> resolved = resolver.resolve(FIELDS, Map.of(), PARTNER_ATTRIBUTES::get);

        assertThat(resolved).containsEntry("headline", "Sommerangebote");
    }
}
