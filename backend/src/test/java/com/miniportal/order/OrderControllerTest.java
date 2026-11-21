package com.miniportal.order;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    private static final String PASSWORD = "demo";
    private static final int RESPONSE_FIELD_COUNT = 12;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestDataChangeRepository testDataChangeRepository;

    private ResultActions placeOrder(String username, String body) throws Exception {
        return mockMvc.perform(post("/api/orders")
                .with(httpBasic(username, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private long placeOrderAndGetId(String username, String body) throws Exception {
        String response = placeOrder(username, body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions getOrder(String username, long orderId) throws Exception {
        return mockMvc.perform(get("/api/orders/{id}", orderId).with(httpBasic(username, PASSWORD)));
    }

    private void expectBalance(String username, long balanceCents) throws Exception {
        mockMvc.perform(get("/api/partners/me").with(httpBasic(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(balanceCents));
    }

    private void expectProblem(ResultActions result, int status, String detail) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    private void expectNothingSaved() {
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void ak3_partnerPlacesOrderAndBalanceIsReducedBySubsidy() throws Exception {
        placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "headline": "Frühlingsaktion!" } }
                """)
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "templateId": 1,
                          "templateName": "Flyer Frühling",
                          "partnerId": 1,
                          "quantity": 500,
                          "status": "BESTELLT",
                          "totalCents": 6000,
                          "subsidyCents": 3000,
                          "ownShareCents": 3000
                        }
                        """, JsonCompareMode.LENIENT))
                .andExpect(jsonPath("$.*", hasSize(RESPONSE_FIELD_COUNT)))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.updatedAt").isString())
                .andExpect(jsonPath("$.values", equalTo(Map.of(
                        "claim", "Gemeinsam mehr erreichen.",
                        "companyName", "Autohaus Müller",
                        "headline", "Frühlingsaktion!"))));

        expectBalance("partner1", 7_000);
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void ak3_missingValuesUsesDefaultValues() throws Exception {
        placeOrder("partner1", """
                { "templateId": 2, "quantity": 10 }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalCents").value(2_500))
                .andExpect(jsonPath("$.subsidyCents").value(1_000))
                .andExpect(jsonPath("$.ownShareCents").value(1_500))
                .andExpect(jsonPath("$.values", equalTo(Map.of(
                        "claim", "Gemeinsam mehr erreichen.",
                        "companyName", "Autohaus Müller",
                        "phone", "09531 12345",
                        "headline", "Sommerangebote"))));

        expectBalance("partner1", 9_000);
    }

    @Test
    void ak3_orderIsAcceptedWhenBalanceEqualsSubsidy() throws Exception {
        placeOrder("partner2", """
                { "templateId": 2, "quantity": 5, "values": {} }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partnerId").value(2))
                .andExpect(jsonPath("$.subsidyCents").value(500));

        expectBalance("partner2", 0);
    }

    @Test
    void ak4_valueForFixedFieldIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "claim": "Anderer Claim" } }
                """), 400, "Feld 'claim' ist nicht änderbar.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
    }

    @Test
    void ak4_valueForPartnerDataFieldIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "companyName": "Andere Firma" } }
                """), 400, "Feld 'companyName' ist nicht änderbar.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
    }

    @Test
    void ak4_unknownKeyIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "foo": "bar" } }
                """), 400, "Feld 'foo' gibt es in Vorlage 1 nicht.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
    }

    @Test
    void ak5_valueLongerThanMaxLengthIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "headline": "%s" } }
                """.formatted("x".repeat(41))), 400, "Feld 'headline' darf höchstens 40 Zeichen lang sein.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void ak6_quantityBelowOneIsRejected(int quantity) throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": %d }
                """.formatted(quantity)), 400, "Menge muss mindestens 1 sein.");

        expectNothingSaved();
    }

    @Test
    void ak6_missingQuantityIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1 }
                """), 400, "Menge fehlt.");

        expectNothingSaved();
    }

    @Test
    void ak6_missingTemplateIdIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "quantity": 500 }
                """), 400, "templateId fehlt.");

        expectNothingSaved();
    }

    @Test
    void ak6_nullValueIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "headline": null } }
                """), 400, "Werte in values dürfen nicht null sein.");

        expectNothingSaved();
    }

    @Test
    void ak6_malformedJsonIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 1, "quantity": "abc" }
                """), 400, "Request-Body ist ungültig.");

        expectNothingSaved();
    }

    @Test
    void ak7_orderIsRejectedWhenBalanceIsInsufficient() throws Exception {
        expectProblem(placeOrder("partner2", """
                { "templateId": 1, "quantity": 500 }
                """), 422, "Guthaben reicht nicht für den Zuschuss von 3000 Cent.");

        expectNothingSaved();
        expectBalance("partner2", 500);
    }

    @Test
    void ak8_orderKeepsValuesAfterPartnerAndTemplateChange() throws Exception {
        long orderId = placeOrderAndGetId("partner1", """
                { "templateId": 1, "quantity": 500 }
                """);

        assertThat(testDataChangeRepository.changeCompanyName(1L, "Autohaus Neu")).isEqualTo(1);
        assertThat(testDataChangeRepository.changeDefaultValue(1L, "claim", "Neuer Claim")).isEqualTo(1);
        assertThat(testDataChangeRepository.changeDefaultValue(1L, "headline", "Neue Überschrift")).isEqualTo(1);

        getOrder("partner1", orderId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.values", equalTo(Map.of(
                        "claim", "Gemeinsam mehr erreichen.",
                        "companyName", "Autohaus Müller",
                        "headline", "Jetzt vorbeikommen!"))));
    }

    @Test
    void ak9_zentraleCannotPlaceOrder() throws Exception {
        expectProblem(placeOrder("zentrale", """
                { "templateId": 1, "quantity": 1 }
                """), 403, "Keine Berechtigung für diese Aktion.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
        expectBalance("partner2", 500);
    }

    @Test
    void ak12_partnerGetsOwnOrder() throws Exception {
        long orderId = placeOrderAndGetId("partner1", """
                { "templateId": 1, "quantity": 500, "values": { "headline": "Frühlingsaktion!" } }
                """);

        getOrder("partner1", orderId)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "id": %d,
                          "templateId": 1,
                          "templateName": "Flyer Frühling",
                          "partnerId": 1,
                          "quantity": 500,
                          "status": "BESTELLT",
                          "totalCents": 6000,
                          "subsidyCents": 3000,
                          "ownShareCents": 3000,
                          "values": {
                            "claim": "Gemeinsam mehr erreichen.",
                            "companyName": "Autohaus Müller",
                            "headline": "Frühlingsaktion!"
                          }
                        }
                        """.formatted(orderId), JsonCompareMode.LENIENT))
                .andExpect(jsonPath("$.*", hasSize(RESPONSE_FIELD_COUNT)));
    }

    @Test
    void ak12_partnerGetsNotFoundForForeignOrder() throws Exception {
        long foreignOrderId = placeOrderAndGetId("partner2", """
                { "templateId": 1, "quantity": 1 }
                """);

        expectProblem(getOrder("partner1", foreignOrderId), 404, "Bestellung " + foreignOrderId + " nicht gefunden.");
    }

    @Test
    void ak12_zentraleGetsAnyOrder() throws Exception {
        long orderId = placeOrderAndGetId("partner1", """
                { "templateId": 1, "quantity": 500 }
                """);

        getOrder("zentrale", orderId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.partnerId").value(1));
    }

    @Test
    void ak12_unknownOrderIdReturnsNotFound() throws Exception {
        expectProblem(getOrder("partner1", 999_999), 404, "Bestellung 999999 nicht gefunden.");
        expectProblem(getOrder("zentrale", 999_999), 404, "Bestellung 999999 nicht gefunden.");
    }

    @Test
    void ak16_unknownTemplateIdIsRejected() throws Exception {
        expectProblem(placeOrder("partner1", """
                { "templateId": 99, "quantity": 1 }
                """), 404, "Vorlage 99 nicht gefunden.");

        expectNothingSaved();
        expectBalance("partner1", 10_000);
    }
}
