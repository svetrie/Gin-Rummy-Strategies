package com.example;

public class AggressivePlayerStrategy extends CautiousPlayerStrategy {

    private static int MAX_DEADWOOD_TOTAL_TO_KNOCK = 10;

    @Override
    public boolean knock() {
        return getTotalDeadwood() <= MAX_DEADWOOD_TOTAL_TO_KNOCK;
    }
}
