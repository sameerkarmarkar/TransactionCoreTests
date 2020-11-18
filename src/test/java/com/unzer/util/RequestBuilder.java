package com.unzer.util;

import com.unzer.constants.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.hpcsoft.adapter.payonxml.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.xml.namespace.QName;

import static com.unzer.chef.DataChef.*;

@AllArgsConstructor
@NoArgsConstructor
public class RequestBuilder {
    private RequestType requestType;
    private static final QName _Request_QNAME = new QName("", "Request");

    public static RequestBuilder newRequest(RequestType requestType) {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.requestType = requestType;
        return requestBuilder;
    }

    public static RequestBuilder newRequest() {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.requestType = new RequestType();
        return requestBuilder;
    }

    public static RequestType preauthorization(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.PREAUTHORIZATION)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType preauthorization(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency, TransactionMode mode) {

        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.PREAUTHORIZATION)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public static RequestType debit(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.DEBIT)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType debit(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency, TransactionMode mode) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.DEBIT)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public static RequestType refund(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.REFUND)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType refund(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency, TransactionMode mode) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.REFUND)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public static RequestType capture(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.CAPTURE)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType capture(Merchant merchant, PaymentMethod paymentMethod, String amount, String currency, TransactionMode mode) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.CAPTURE)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public static RequestType register(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.REGISTERATION)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType register(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency, TransactionMode mode) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.REGISTERATION)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public static RequestType schedule(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.SCHEDULE)
                .and().withAmountAndCurrency(amount, currency)
                .build();
    }

    public static RequestType schedule(Merchant merchant, PaymentMethod paymentMethod,String amount, String currency, TransactionMode mode) {
        return newRequest().withMerchant(merchant).withDefaultTransactionInfo()
                .and().withTransactionType(paymentMethod, TransactionCode.SCHEDULE)
                .and().withAmountAndCurrency(amount, currency)
                .and().withMode(mode)
                .build();
    }

    public RequestBuilder withSenderId(String senderId) {
        SecurityType security = new SecurityType();
        security.setSender(senderId);
        HeaderType header = new HeaderType();
        header.setSecurity(security);
        requestType.setHeader(header);
        return this;
    }

    public RequestBuilder and() {
        return this;
    }

    public RequestBuilder withDefaultTransactionInfo() {
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();

        transaction.setMode(TransactionMode.INTEGRATOR_TEST.name());
        transaction.setResponse("SYNC");
        transaction.setCustomer(customerType());
        requestType.setTransaction(transaction);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withMode(TransactionMode mode) {
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();
        transaction.setMode(mode.name());
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withChannel(String channelId) {
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();
        transaction.setChannel(channelId);
        requestType.setTransaction(transaction);
        return this;

    }

    public RequestBuilder withUser(String username, String password) {
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();
        UserType user = new UserType();
        user.setLogin(username);
        user.setPwd(password);
        transaction.setUser(user);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withAmountAndCurrency(String amount, String currency) {
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();
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
        return this;
    }

    public RequestBuilder withAmount(String amount) {
        return withAmountAndCurrency(amount, null);
    }

    public RequestBuilder withCurrency(String currency) {
        return withAmountAndCurrency(null, currency);
    }

    public RequestBuilder withTransactionType(PaymentMethod paymentMethod, TransactionCode transactionCode) {
        String paymentCode = paymentMethod.getMethod() + "." + transactionCode.getCode();
        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();
        PaymentRequestType paymentType = transaction.getPayment() != null
                ? transaction.getPayment() : new PaymentRequestType();
        paymentType.setCode(paymentCode);
        transaction.setPayment(paymentType);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withCardOnFile(String initiation) {
        TransactionCredentialOnFileType credOnFile = new TransactionCredentialOnFileType();
        credOnFile.setInitiation(initiation);

        TransactionRequestType transaction = requestType.getTransaction() != null
                ? requestType.getTransaction() : new TransactionRequestType();

        transaction.setCredentialOnFile(credOnFile);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestType build() {
        return requestType;
    }

    public RequestBuilder withMerchant(Merchant merchant){
        return this.withSenderId(merchant.getSender())
                .withChannel(merchant.getChannel())
                .withUser(merchant.getUser() , merchant.getPassword());

    }

    public RequestBuilder withCard(Card card) {
        TransactionRequestType transaction = requestType.getTransaction();
        transaction.setAccount(requestAccountType(card));
        AuthenticationType auth = new AuthenticationType();
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withAccountHolder(String accountHolder, String brand) {
        TransactionRequestType transaction = requestType.getTransaction();
        transaction.setAccount(requestAccountType(accountHolder, "DE", brand));
        AuthenticationType auth = new AuthenticationType();
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withResponseUrl() {
        TransactionRequestType transaction = requestType.getTransaction();
        FrontendType frontEndType = new FrontendType();
        frontEndType.setResponseUrl("https://web.dev.hpchd.loc/testtool/response;jsessionid=F10438C50B5E4985731E4F6085DBF2B5.testtools01");
        transaction.setFrontend(frontEndType);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder referringTo(ResponseType parent) {
        TransactionRequestType transaction = requestType.getTransaction();
        IdentificationRequestType identification = transaction.getIdentification() != null
                ? transaction.getIdentification() : new IdentificationRequestType();
        identification.setReferenceID(parent.getTransaction().getIdentification().getUniqueID());
        transaction.setIdentification(identification);

        AccountRequestType account = transaction.getAccount() != null ? transaction.getAccount() : new AccountRequestType();
        account.setNumber(parent.getTransaction().getAccount().getNumber());
        transaction.setAccount(account);
        transaction.setCustomer(null);

        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder referringTo(String parentTransactionId) {
        TransactionRequestType transaction = requestType.getTransaction();
        IdentificationRequestType identification = transaction.getIdentification() != null
                ? transaction.getIdentification() : new IdentificationRequestType();
        identification.setReferenceID(parentTransactionId);
        transaction.setIdentification(identification);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withRecurrance(Recurrence recurrence) {
        TransactionRequestType transaction = requestType.getTransaction();
        PaymentRequestType payment = transaction.getPayment();
        RecurrenceType recurrenceType = new RecurrenceType();
        recurrenceType.setMode(recurrence.name());
        payment.setRecurrence(recurrenceType);
        transaction.setPayment(payment);
        requestType.setTransaction(transaction);
        return this;
    }

    public RequestBuilder withSchedule(TransactionCode transactionCode) {
        TransactionRequestType transaction = requestType.getTransaction();

        JobType job = new JobType();
        ActionType action = new ActionType();
        action.setType(transactionCode.getCode());

        ExecutionType execution = new ExecutionType();
        execution.setExpression(CronHelper.createNew().afterSeconds(30).inCurrentMinute().and().inCurrentHour().getExpression());

        DurationType duration = new DurationType();
        duration.setNumber(1);
        duration.setUnit("HOUR");

        job.setName("Test_Schedule_"+RandomStringUtils.randomAlphanumeric(5));
        job.setStart(CronHelper.createNew().getUtcDate());
        job.setAction(action);
        job.setExecution(execution);
        job.setDuration(duration);

        transaction.setJob(job);
        return this;

    }

}
