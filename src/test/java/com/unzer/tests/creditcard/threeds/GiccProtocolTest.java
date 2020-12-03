package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.tests.BaseTest;
import com.unzer.helpers.DatabaseHelper;
import com.unzer.util.Flow;
import com.unzer.verifiers.GiccVerifier;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.unzer.matchers.TransactionProcessingMatches.transactionProcessingMatches;

public class GiccProtocolTest implements BaseTest {

    private static final GiccVerifier GICC_VERIFIER = GiccVerifier.INSTANCE;

    @ParameterizedTest(name = "{0}")
    @MethodSource("oneOffTransactions")
    @DisplayName("Transaction core should send correct values in the GICC message for one off transaction")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForOneOffThreedsTwoTransaction(String description, Flow flow) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        RequestType request = flow.getLastTransactionRequest();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(request.getTransaction().getAccount().getBrand());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("returningCustomer")
    @DisplayName("Transaction core should send correct values in the GICC message for returning customer")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForReturningCustomerThreedsTwoTransaction(String description, Flow flow, String cardBrand) {
        flow.execute();
        RequestType request = flow.getLastTransactionRequest();
        ResponseType response = flow.getLastTransactionResponse();
        ResponseType parentResponse = flow.getParentResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        String parentShortId = parentResponse.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForReturningCustomer(cardBrand, parentShortId);
    }

    @ParameterizedTest
    @MethodSource("cards")
    @DisplayName("Transaction core should send correct values in the GICC message for initial recurring transaction")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledInitialRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds();
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));

        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @DisplayName("Transaction core should send correct values in the GICC message for unscheduled repeated recurring transaction")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledRepeatedRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds()
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.REPEATED);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForUnscheduledSubsequentRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @DisplayName("Transaction core should send correct values in the GICC message for scheduled repeated recurring transaction")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForSubsequentScheduledThreedsTwoTransaction(Card card, Merchant merchant) {

        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).withResponseUrl().and().asThreeds()
                .then().schedule().withSchedule(TransactionCode.DEBIT).referringToNth(TransactionCode.REGISTERATION);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        String scheduledTransaction  = DatabaseHelper.getScheduledTransactionShortId(shortId);
        GICC_VERIFIER.withShortId(scheduledTransaction).getMessage().and().verifyFieldsForScheduledSubsequentRecurring(card.getCardBrand());
    }

    private static Stream<Arguments> oneOffTransactions() {
        return Stream.of(
                Arguments.of("One off preauth with Mastercard",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().preauthorization().withCard(Card.MASTERCARD_4).asThreeds().withResponseUrl())/*,
                Arguments.of("One off debit with Visa",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().debit().withCard(Card.VISA_1).asThreeds().withResponseUrl())*/
        );
    }

    private static Stream<Arguments> returningCustomer() {
        return Stream.of(
                Arguments.of("DEBIT >> REFUND with MASTERCARD", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().debit().withAmount("10").withCard(Card.MASTERCARD_4).asThreeds().withResponseUrl()
                        .then().refund().withAmount("10").referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("REGISTER >> DEBIT >> partial REFUND", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().register().withCard(Card.MASTERCARD_4)
                        .then().debit().withAmount("20").referringToNth(TransactionCode.REGISTERATION).asThreeds().withResponseUrl()
                        .then().refund().withAmount("10").referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("PREAUTH >> CAPTURE >> REFUND", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().preauthorization().withAmount("100").withCard(Card.MASTERCARD_4).asThreeds().withResponseUrl()
                        .then().capture().withAmount("100").referringToNth(TransactionCode.PREAUTHORIZATION)
                        .then().refund().withAmount("100").referringToNth(TransactionCode.CAPTURE), "MASTER")
        );
    }

    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.MASTERCARD_4, Merchant.SIX_THREEDS_TWO_MERCHANT)/*,
                Arguments.of(Card.VISA_7, Merchant.SIX_THREEDS_TWO_MERCHANT)*/
        );
    }

}
