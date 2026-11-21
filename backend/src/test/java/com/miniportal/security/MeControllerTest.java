package com.miniportal.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ak20_partnerGetsOwnUsernameAndRole() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("partner1", "demo")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "username": "partner1", "role": "PARTNER" }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void ak20_zentraleGetsOwnUsernameAndRole() throws Exception {
        mockMvc.perform(get("/api/me").with(httpBasic("zentrale", "demo")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "username": "zentrale", "role": "ZENTRALE" }
                        """, JsonCompareMode.STRICT));
    }
}
