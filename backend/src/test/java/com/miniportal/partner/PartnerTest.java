package com.miniportal.partner;

import com.miniportal.common.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerTest {

    private static Partner partnerWithBalance(long balanceCents) {
        return new Partner("Autohaus Müller", "Hauptstr. 1", "96106", "Ebern", "09531 12345", balanceCents);
    }

    @Test
    void ak3_deductBalanceReducesBalance() {
        Partner partner = partnerWithBalance(10_000);

        partner.deductBalance(3_000);

        assertThat(partner.getBalanceCents()).isEqualTo(7_000);
    }

    @Test
    void ak3_balanceEqualToAmountCanBeDeducted() {
        Partner partner = partnerWithBalance(500);

        partner.deductBalance(500);

        assertThat(partner.getBalanceCents()).isZero();
    }

    @Test
    void ak7_deductBalanceRejectsInsufficientBalanceAndKeepsBalance() {
        Partner partner = partnerWithBalance(500);

        assertThatThrownBy(() -> partner.deductBalance(3_000))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Guthaben reicht nicht für den Zuschuss von 3000 Cent.");
        assertThat(partner.getBalanceCents()).isEqualTo(500);
    }
}
