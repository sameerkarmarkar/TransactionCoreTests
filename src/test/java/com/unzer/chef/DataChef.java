package com.unzer.chef;

import com.unzer.constants.Card;
import net.hpcsoft.adapter.payonxml.*;
import org.apache.commons.lang3.RandomStringUtils;

public class DataChef {
    public static AccountRequestType requestAccountType() {
        AccountRequestType account = new AccountRequestType();
        account.setHolder("Test Account");
        account.setBrand("VISA");
        account.setNumber("4761739090000088");
        account.setExpiry(expiryType());
        account.setVerification("123");
        return account;
    }

    public static AccountRequestType requestAccountType(Card card) {
        AccountRequestType account = new AccountRequestType();
        account.setHolder(card.getCardHolder());
        account.setBrand(card.getCardBrand());
        account.setNumber(card.getCardNumber());
        account.setExpiry(expiryType(card.getExpiryMonth(), card.getExpiryYear()));
        account.setVerification("123");
        return account;
    }

    public static ExpiryType expiryType() {
        ExpiryType expiry = new ExpiryType();
        expiry.setMonth("12");
        expiry.setYear("2025");
        return expiry;
    }

    public static ExpiryType expiryType(String expiryMonth, String expiryYear) {
        ExpiryType expiry = new ExpiryType();
        expiry.setMonth(expiryMonth);
        expiry.setYear(expiryYear);
        return expiry;
    }

    public static CustomerType customerType() {
        CustomerType customer = new CustomerType();
        customer.setName(nameType());
        customer.setAddress(addressType());
        customer.setContact(contactType());
        return customer;
    }

    public static NameType nameType() {
        NameType name = new NameType();
        name.setGiven("Test");
        name.setFamily("Account");
        return name;
    }

    public static AddressType addressType() {
        AddressType address = new AddressType();
        address.setStreet("Grasbrunn");
        address.setZip("85630");
        address.setCity("Munich");
        address.setCountry("DE");
        return address;
    }

    public static ContactType contactType() {
        ContactType contact = new ContactType();
        contact.setEmail("test@unzer.com");
        contact.setIp("12.12.12.12");
        return contact;
    }

    public static PaymentRequestType paymentRequestType() {
        PaymentRequestType payment = new PaymentRequestType();
        payment.setCode("CC.PA");
        payment.setPresentation(presentationType());
        return payment;
    }

    public static PresentationType presentationType() {
        PresentationType presentation = new PresentationType();
        presentation.setAmount("121.45");
        presentation.setCurrency("CHF");
        presentation.setUsage(RandomStringUtils.randomAlphanumeric(10));
        return presentation;
    }

}
