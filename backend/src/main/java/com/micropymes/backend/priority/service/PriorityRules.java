package com.micropymes.backend.priority.service;

import com.micropymes.backend.priority.domain.PriorityLevel;
import org.springframework.stereotype.Component;

@Component
public class PriorityRules {

    public static final int MAX_SCORE = 100;

    public PriorityLevel levelFor(int score) {

        if (score >= 70) {
            return PriorityLevel.URGENT;
        }

        if (score >= 45) {
            return PriorityLevel.HIGH;
        }

        if (score >= 20) {
            return PriorityLevel.MEDIUM;
        }

        return PriorityLevel.LOW;
    }

    public int cap(int score) {
        return Math.min(score, MAX_SCORE);
    }
}