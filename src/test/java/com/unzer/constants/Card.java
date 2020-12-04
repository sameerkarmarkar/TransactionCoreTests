package com.unzer.constants;

import lombok.Getter;

@Getter
public enum Card {
    VISA_1("4711100000007237", "VISA", "12", "2022", "123", "Test Account"),
    VISA_2("4761739090000088","VISA","12","2022","729", "Test Account"),
    VISA_3("4012001037141112", "VISA", "12", "2027", "212", "Test Account"),
    VISA_4("4012001037167778", "VISA", "12", "2022", "123", "Test Account"),
    VISA_5("4003028255459216", "VISA", "12", "2024", "123", "Test Account"),
    VISA_6("4485276145112951", "VISA", "12", "2024", "123", "Test Account"),
    VISA_7("4015500000001234", "VISA", "12", "2025", "659", "Test Account"),
    VISA_8("4761739001010119", "VISA", "12", "2025", "157", "Test Account"),
    VISA_9("4761739001011133", "VISA", "12", "2025", "157", "Test Account"),
    VISA_10("4761739001010010", "VISA", "12", "2025", "157", "Test Account"),
    VISA_11("4059200000000016", "VISA", "12", "2025", "157", "Test Account"),
    VISA_12("4761739090000096", "VISA", "12", "2022", "461", "Test Account"),
    MASTERCARD_1("5453010000059543", "MASTER","11","2024","123", "Test Accoünt"),
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
