package com.unzer.chef;

import com.unzer.constants.Merchant;
import com.unzer.constants.PaymentMethod;
import com.unzer.constants.TransactionMode;
import com.unzer.constants.TransactionCode;
import com.unzer.util.RequestBuilder;
import net.hpcsoft.adapter.payonxml.*;
import org.apache.commons.lang3.RandomStringUtils;

import static com.unzer.constants.Constants.*;


public class RequestChef {

    private static RequestType requestType = basicRequest();

    public static RequestType preauthorization(Merchant merchant, String amount, String curreny) {
        updateMerchantCredentials(merchant);
        updateTransactionType(TransactionCode.PREAUTHORIZATION);
        return requestType;
    }

    public static RequestType preautorization(String amount, String currency, Merchant merchant, PaymentMethod paymentMethod) {
        return RequestBuilder.newRequest()
                .and().withSenderId(merchant.getSender())
                .and().withDefaultTransactionInfo()
                .and().withChannel(merchant.getChannel()).and().withUser(merchant.getUser(), merchant.getPassword())
                .and().withAmountAndCurrency(amount, currency)
                .and().withTransactionType(paymentMethod, TransactionCode.PREAUTHORIZATION)
                .build();
    }

    public static RequestType capture(String amount, String currency, PaymentMethod paymentMethod) {
        return RequestBuilder.newRequest()
                .and().withSenderId(SENDER_ID)
                .and().withDefaultTransactionInfo()
                .and().withChannel(CHANNEL).and().withUser(USERNAME, PASSWORD)
                .and().withAmountAndCurrency(amount, currency)
                .and().withTransactionType(paymentMethod, TransactionCode.CAPTURE)
                .build();
    }

    public static RequestType reversal(String amount, String currency, PaymentMethod paymentMethod) {
        return RequestBuilder.newRequest()
                .and().withSenderId(SENDER_ID)
                .and().withDefaultTransactionInfo()
                .and().withChannel(CHANNEL).and().withUser(USERNAME, PASSWORD)
                .and().withAmountAndCurrency(amount, currency)
                .and().withTransactionType(paymentMethod, TransactionCode.REFUND)
                .build();
    }

    private static RequestType basicRequest() {
        RequestType requestType = new RequestType();
        requestType.setHeader(new HeaderType());
        TransactionRequestType transaction = new TransactionRequestType();
        transaction.setMode(TransactionMode.INTEGRATOR_TEST.name());
        transaction.setResponse("SYNC");
        transaction.setAccount(DataChef.requestAccountType());
        requestType.setTransaction(transaction);
        return requestType;
    }

    private static RequestType updateMerchantCredentials(Merchant merchant) {
        requestType.setHeader(header(merchant.getSender()));
        TransactionRequestType transaction = requestType.getTransaction();
        transaction.setChannel(merchant.getChannel());
        transaction.setUser(user(merchant.getUser(), merchant.getPassword()));
        requestType.setTransaction(transaction);
        return requestType;
    }

    private static HeaderType header(String sender) {
        SecurityType security = new SecurityType();
        security.setSender(sender);
        HeaderType header = new HeaderType();
        header.setSecurity(security);
        return header;
    }

    private static UserType user(String username, String password) {
        UserType user = new UserType();
        user.setLogin(username);
        user.setPwd(password);
        return user;
    }

    private static void updateTransactionType(TransactionCode transactionCode) {
        TransactionRequestType transaction = requestType.getTransaction();
        transaction.setPayment(paymentType(transactionCode));
        requestType.setTransaction(transaction);
    }

    private static PaymentRequestType paymentType(TransactionCode transactionCode) {
        PaymentRequestType paymentType = new PaymentRequestType();
        paymentType.setCode(transactionCode.getCode());
        return  paymentType;
    }

    private static void setAmountAndCurrency(String amount, String currency) {
        TransactionRequestType transaction = requestType.getTransaction();
        PaymentRequestType paymentType = transaction.getPayment() != null
                ? transaction.getPayment() : new PaymentRequestType();
        PresentationType presentationType = paymentType.getPresentation() != null?
                paymentType.getPresentation() : new PresentationType();
        amount = amount != null ? amount : "100";
        presentationType.setAmount(amount);
        currency = currency != null ? currency : "EUR";
        presentationType.setCurrency(currency);
        presentationType.setUsage(RandomStringUtils.randomAlphanumeric(10));
        paymentType.setPresentation(presentationType);
        transaction.setPayment(paymentType);
        requestType.setTransaction(transaction);
    }


}
