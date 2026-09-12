package com.micropymes.backend.priority.service;

import com.micropymes.backend.priority.domain.PriorityLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriorityRulesTest {

    private final PriorityRules rules =
            new PriorityRules();

    @Test
    void scoreBelow20IsLow() {
        assertThat(
                rules.levelFor(19)
        ).isEqualTo(PriorityLevel.LOW);
    }

    @Test
    void score20IsMedium() {
        assertThat(
                rules.levelFor(20)
        ).isEqualTo(PriorityLevel.MEDIUM);
    }

    @Test
    void score45IsHigh() {
        assertThat(
                rules.levelFor(45)
        ).isEqualTo(PriorityLevel.HIGH);
    }

    @Test
    void score70IsUrgent() {
        assertThat(
                rules.levelFor(70)
        ).isEqualTo(PriorityLevel.URGENT);
    }

    @Test
    void scoreCannotExceed100() {
        assertThat(
                rules.cap(135)
        ).isEqualTo(100);
    }

    @Test
    void scoreBelow100IsNotModified() {
        assertThat(
                rules.cap(87)
        ).isEqualTo(87);
    }
}