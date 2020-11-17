package com.unzer.constants;

public enum TransactionCode {
    PREAUTHORIZATION("PA"),
    CAPTURE("CP"),
    REFUND("RF"),
    DEBIT("DB"),
    REGISTERATION("RG"),
    SCHEDULE("SD");

    private final String code;

    private TransactionCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    /*public String getSubCode() {
        return this.getCode().split("\\.")[1];
    }*/

}
