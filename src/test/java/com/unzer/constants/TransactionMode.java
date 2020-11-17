package com.unzer.constants;

public enum TransactionMode {
    CONNECTOR_TEST,
    INTEGRATOR_TEST;

    public static TransactionMode as(String testMode) {
        return TransactionMode.valueOf(testMode);
    }
}