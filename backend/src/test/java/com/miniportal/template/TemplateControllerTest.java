package com.miniportal.template;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ak1_partnerGetsAllTemplatesWithFields() throws Exception {
        mockMvc.perform(get("/api/templates").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name").value(contains("Flyer Frühling", "Plakat Sommer", "Zeitungsanzeige Herbst")))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("FLYER"))
                .andExpect(jsonPath("$[0].unitPriceCents").value(12))
                .andExpect(jsonPath("$[0].subsidyPercent").value(50))
                .andExpect(jsonPath("$[0].fields", hasSize(3)))
                .andExpect(jsonPath("$[0].fields[0].key").value("claim"))
                .andExpect(jsonPath("$[0].fields[0].label").value("Marken-Claim"))
                .andExpect(jsonPath("$[0].fields[0].fieldType").value("FIXED"))
                .andExpect(jsonPath("$[0].fields[0].defaultValue").value("Gemeinsam mehr erreichen."))
                .andExpect(jsonPath("$[0].fields[0].partnerAttribute").value(nullValue()))
                .andExpect(jsonPath("$[0].fields[0].maxLength").value(nullValue()))
                .andExpect(jsonPath("$[0].fields[0].position").value(1))
                .andExpect(jsonPath("$[0].fields[1].key").value("companyName"))
                .andExpect(jsonPath("$[0].fields[1].label").value("Firmenname"))
                .andExpect(jsonPath("$[0].fields[1].fieldType").value("PARTNER_DATA"))
                .andExpect(jsonPath("$[0].fields[1].defaultValue").value(nullValue()))
                .andExpect(jsonPath("$[0].fields[1].partnerAttribute").value("companyName"))
                .andExpect(jsonPath("$[0].fields[1].maxLength").value(nullValue()))
                .andExpect(jsonPath("$[0].fields[1].position").value(2))
                .andExpect(jsonPath("$[0].fields[2].key").value("headline"))
                .andExpect(jsonPath("$[0].fields[2].label").value("Überschrift"))
                .andExpect(jsonPath("$[0].fields[2].fieldType").value("EDITABLE"))
                .andExpect(jsonPath("$[0].fields[2].defaultValue").value("Jetzt vorbeikommen!"))
                .andExpect(jsonPath("$[0].fields[2].partnerAttribute").value(nullValue()))
                .andExpect(jsonPath("$[0].fields[2].maxLength").value(40))
                .andExpect(jsonPath("$[0].fields[2].position").value(3));
    }

    @Test
    void ak1_zentraleGetsAllTemplatesWithFields() throws Exception {
        mockMvc.perform(get("/api/templates").with(httpBasic("zentrale", "demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].fields", hasSize(3)))
                .andExpect(jsonPath("$[1].fields", hasSize(4)))
                .andExpect(jsonPath("$[2].fields", hasSize(4)));
    }

    @Test
    void ak1_templatesAreSortedByIdAndFieldsByPosition() throws Exception {
        mockMvc.perform(get("/api/templates").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(1, 2, 3)))
                .andExpect(jsonPath("$[0].fields[*].position").value(contains(1, 2, 3)))
                .andExpect(jsonPath("$[1].fields[*].position").value(contains(1, 2, 3, 4)))
                .andExpect(jsonPath("$[2].fields[*].position").value(contains(1, 2, 3, 4)));
    }

    @Test
    void ak1_eachTemplateHasAtLeastOneFieldOfEachType() throws Exception {
        var result = mockMvc.perform(get("/api/templates").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk());
        for (int i = 0; i < 3; i++) {
            for (String fieldType : new String[] {"FIXED", "PARTNER_DATA", "EDITABLE"}) {
                result.andExpect(jsonPath("$[%d].fields[?(@.fieldType == '%s')]".formatted(i, fieldType)).isNotEmpty());
            }
        }
    }

    @Test
    void ak19_returnsTemplateById() throws Exception {
        mockMvc.perform(get("/api/templates/2").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 2,
                          "name": "Plakat Sommer",
                          "type": "POSTER",
                          "unitPriceCents": 250,
                          "subsidyPercent": 40,
                          "fields": [
                            { "key": "claim", "label": "Marken-Claim", "fieldType": "FIXED",
                              "defaultValue": "Gemeinsam mehr erreichen.", "partnerAttribute": null,
                              "maxLength": null, "position": 1 },
                            { "key": "companyName", "label": "Firmenname", "fieldType": "PARTNER_DATA",
                              "defaultValue": null, "partnerAttribute": "companyName",
                              "maxLength": null, "position": 2 },
                            { "key": "phone", "label": "Telefon", "fieldType": "PARTNER_DATA",
                              "defaultValue": null, "partnerAttribute": "phone",
                              "maxLength": null, "position": 3 },
                            { "key": "headline", "label": "Überschrift", "fieldType": "EDITABLE",
                              "defaultValue": "Sommerangebote", "partnerAttribute": null,
                              "maxLength": 30, "position": 4 }
                          ]
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void ak19_returns404ForUnknownTemplateId() throws Exception {
        mockMvc.perform(get("/api/templates/999").with(httpBasic("partner1", "demo")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Vorlage 999 nicht gefunden."));
    }
}
