package com.unzer.constants;

public enum TransactionType {
    PREAUTH("CC.PA"),
    CAPTURE("CC.CP"),
    REFUND("CC.RF"),
    DEBIT("CC.DB"),
    REGISTRATION("CC.RG"),
    SCHEDULE("CC.SD");

    private final String code;

    private TransactionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public String getSubCode() {
        return this.getCode().split("\\.")[1];
    }

}
