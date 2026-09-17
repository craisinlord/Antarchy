package com.craisinlord.antarchy.content.effect;

/** Synchronized, presentation-only state for the short-lived Royal Judgment mark. */
public interface JudgmentMarkAccess {
    boolean antarchy$isJudgmentMarked();

    void antarchy$setJudgmentMarked(boolean marked);
}
