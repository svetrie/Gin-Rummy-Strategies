package com.example;

public class CautiousPlayerStrategy extends AggressivePlayerStrategy {

    private static int MAX_DEADWOOD_TOTAL_TO_KNOCK = 0;

    @Override
    public boolean knock() {
        return getTotalDeadwood() <= MAX_DEADWOOD_TOTAL_TO_KNOCK;
    }
}
