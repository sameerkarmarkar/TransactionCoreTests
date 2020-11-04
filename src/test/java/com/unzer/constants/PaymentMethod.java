package com.unzer.constants;

public enum PaymentMethod {
    CREDITCARD("CC"),
    INVOICE("IV"),
    DEBITCARD("DB"),
    ALTPAY("AL");

    private final String method;

    private PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }
}
