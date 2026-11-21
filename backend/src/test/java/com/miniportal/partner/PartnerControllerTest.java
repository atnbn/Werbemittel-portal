package com.miniportal.partner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ak21_partnerGetsOwnMasterDataAndBalance() throws Exception {
        mockMvc.perform(get("/api/partners/me").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 1,
                          "companyName": "Autohaus Müller",
                          "street": "Hauptstr. 1",
                          "zip": "96106",
                          "city": "Ebern",
                          "phone": "09531 12345",
                          "balanceCents": 10000
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void ak21_otherPartnerGetsOwnMasterData() throws Exception {
        mockMvc.perform(get("/api/partners/me").with(httpBasic("partner2", "demo")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": 2,
                          "companyName": "Fahrrad Schmidt",
                          "street": "Bahnhofstr. 5",
                          "zip": "96047",
                          "city": "Bamberg",
                          "phone": "0951 98765",
                          "balanceCents": 500
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void ak21_zentraleIsForbidden() throws Exception {
        mockMvc.perform(get("/api/partners/me").with(httpBasic("zentrale", "demo")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Keine Berechtigung für diese Aktion."));
    }
}
