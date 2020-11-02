package com.unzer.constants;

import lombok.Getter;

@Getter
public enum Card {
    VISA("4761739090000088","VISA","11","2025","123", "Test Account"),
    MASTERCARD("5453010000059543", "MASTER","11","2024","123", "Test Account");

    private String cardNumber;
    private String cardBrand;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardHolder;

    Card(String cardNumber, String cardBrand, String expiryMonth, String expiryYear, String cvv, String cardHolder) {
        this.cardNumber = cardNumber;
        this.cardBrand = cardBrand;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.cardHolder = cardHolder;
    }

}
