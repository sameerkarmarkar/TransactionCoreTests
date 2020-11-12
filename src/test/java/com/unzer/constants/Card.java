package com.unzer.constants;

import lombok.Getter;

@Getter
public enum Card {
    VISA_1("4761739090000088","VISA","11","2025","123", "Test Account"),
    VISA_2("4012001037141112", "VISA", "12", "2027", "212", "Test Account"),
    MASTERCARD_1("5453010000059543", "MASTER","11","2024","123", "Test Account"),
    MASTERCARD_2("5266248793296818", "MASTER", "12", "2024", "123", "Test Account"),
    MASTERCARD_3("5266009957695322", "MASTER", "12", "2024", "123", "Test Account"),
    MASTERCARD_4("5544330000000003", "MASTER", "12", "2024", "123", "Test Account"),
    MASTERCARD_5("5544330000000037", "MASTER", "12", "2024", "123", "Test Account");


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
