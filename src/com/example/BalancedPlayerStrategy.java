package com.example;

public class BalancedPlayerStrategy extends CautiousPlayerStrategy {

    private static final int MAX_DEADWOOD_TOTAL_TO_KNOCK = 5;

    @Override
    public boolean knock() {
        return getTotalDeadwood() <= MAX_DEADWOOD_TOTAL_TO_KNOCK;
    }

}
