package com.unzer.tests.creditcard.threeds.kalixa;

import com.unzer.constants.*;
import com.unzer.helpers.DatabaseHelper;
import com.unzer.tests.BaseTest;
import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.unzer.matchers.TransactionProcessingMatches.transactionProcessingMatches;
import static org.hamcrest.MatcherAssert.assertThat;

public class SuccessfuleThreedsTwoTransactionsTest implements BaseTest {

    private static final Merchant merchant = Merchant.KALIXA_THREEDS_TWO_MERCHANT;

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsTwoPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2).execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullReversalForThreedsTwoPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION)
                .withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialReversalForThreedsTwoPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().reversal().withAmount("20").referringToNth(TransactionCode.PREAUTHORIZATION)
                .withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullCaptureForThreedsTwoPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialCaptureForThreedsTwoPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10")
                .withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullRefundForThreedsTwoCapturePreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50").withSource(source)
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("50").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialRefundForThreedsTwoCapturePreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50").withSource(source)
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("20").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessRefundForParialThreedsTwoCapturePreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10").withSource(source)
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("10").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessReversalForNonCapturedAmount(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10").withSource(source)
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("40").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessCaptureForNonReversedAmount(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10").withSource(source)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("40").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessReversalForThreedsTwoCapturePreauthorizaropn(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50").withSource(source)
                .then().reversal().referringToNth(TransactionCode.CAPTURE).withAmount("50").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }


    @Disabled
    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessRebillForThreedsTwoPreauthorizationCapture(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50").withSource(source)
                .then().rebill().referringToNth(TransactionCode.CAPTURE).withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsTwoDebit(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsTwoCofPreauthorization(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .and().withSource(source)
                .then().preauthorization().withAmount("50").withSource(source)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2).referringToNth(TransactionCode.REGISTERATION)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsTwoCofDebit(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .and().withSource(source)
                .then().debit().withAmount("50").withSource(source)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2).referringToNth(TransactionCode.REGISTERATION)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullRefundForThreedsTwoDebit(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().refund().referringToNth(TransactionCode.DEBIT).withAmount("50").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialRefundForThreedsTwoDebit(Card card, String source) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withSource(source)
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_2)
                .then().refund().referringToNth(TransactionCode.DEBIT).withAmount("10").withSource(source)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }


    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.VISA_5, "XML"),
                Arguments.of(Card.VISA_5, "POST")
        );
    }

}
