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

public class SuccessfuleThreedsOneTransactionsTest extends BaseTest {

    private static final Merchant merchant = Merchant.KALIXA_THREEDS_ONE_MERCHANT;

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsOnePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1).execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullReversalForThreedsOnePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialReversalForThreedsOnePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().reversal().withAmount("20").referringToNth(TransactionCode.PREAUTHORIZATION)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullCaptureForThreedsOnePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialCaptureForThreedsOnePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullRefundForThreedsOneCapturePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("50")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialRefundForThreedsOneCapturePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("20")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessRefundForParialThreedsOneCapturePreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10")
                .then().refund().referringToNth(TransactionCode.CAPTURE).withAmount("10")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessReversalForNonCapturedAmount(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10")
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("40")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessCaptureForNonReversedAmount(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().reversal().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("10")
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("40")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessReversalForThreedsOneCapturePreauthorizaropn(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .then().reversal().referringToNth(TransactionCode.CAPTURE).withAmount("50")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }


    @Disabled
    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessRebillForThreedsOnePreauthorizationCapture(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .then().rebill().referringToNth(TransactionCode.CAPTURE)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsOneDebit(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsOneCofPreauthorization(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().preauthorization().withAmount("50")
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessThreedsOneCofDebit(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withAmount("50")
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessFullRefundForThreedsOneDebit(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().refund().referringToNth(TransactionCode.DEBIT).withAmount("50")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }

    @ParameterizedTest
    @MethodSource("cards")
    public void shouldProcessPartialRefundForThreedsOneDebit(Card card) {
        Flow flow = Flow.forMerchant(merchant).and().withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().debit().withAmount("50")
                .and().withCard(card)
                .and().withResponseUrl()
                .and().asThreeds(ThreedsVersion.VERSION_1)
                .then().refund().referringToNth(TransactionCode.DEBIT).withAmount("10")
                .execute();

        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
    }


    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.MASTERCARD_1),
                Arguments.of(Card.VISA_1)
        );
    }

}
