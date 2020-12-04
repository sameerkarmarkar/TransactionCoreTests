package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.tests.BaseTest;
import com.unzer.helpers.DatabaseHelper;
import com.unzer.util.Flow;
import com.unzer.verifiers.GiccVerifier;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
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
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForOneOffThreedsTwoTransaction(String description, Flow flow) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        RequestType request = flow.getLastTransactionRequest();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(request.getTransaction().getAccount().getBrand());
    }

    @ParameterizedTest(name = "Returning cistomer transaction with {0}")
    @MethodSource("returningCustomer")
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

    @ParameterizedTest(name = "Unscheduled initial recurring transaction with {0}")
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledInitialRecurringTransaction(String description, Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds();
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));

        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(card.getCardBrand());
    }

    @ParameterizedTest(name = "Unscheduled repeated recurring transaction with {0}")
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledRepeatedRecurringTransaction(String description, Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds().execute();

        String initialTransactionShortId = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();

        flow = flow.continueWith().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.REPEATED);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForUnscheduledSubsequentRecurring(card.getCardBrand(), initialTransactionShortId);
    }

    @ParameterizedTest(name = "Scheduled repeated recurring transaction with {0}")
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForSubsequentScheduledThreedsTwoTransaction(String description, Card card, Merchant merchant) {

        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).withResponseUrl().and().asThreeds()
                .execute();
        String initialTransactionShortId = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();

        flow = flow.continueWith().schedule().withSchedule(TransactionCode.DEBIT).referringToNth(TransactionCode.REGISTERATION);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", DatabaseHelper.getTransactionProcessing(shortId), transactionProcessingMatches(TransactionProcessing.SUCCESSFUL));
        String scheduledTransaction  = DatabaseHelper.getScheduledTransactionShortId(shortId);
        GICC_VERIFIER.withShortId(scheduledTransaction).getMessage().and().verifyFieldsForScheduledSubsequentRecurring(card.getCardBrand(), initialTransactionShortId);
    }

    private static Stream<Arguments> oneOffTransactions() {
        return Stream.of(
                Arguments.of("One off preauth with Mastercard",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().preauthorization().withCard(Card.MASTERCARD_1).asThreeds().withResponseUrl()),
                Arguments.of("One off debit with Visa",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().debit().withCard(Card.VISA_8).asThreeds().withResponseUrl())
        );
    }

    private static Stream<Arguments> returningCustomer() {
        return Stream.of(
                Arguments.of("DEBIT >> REFUND with MASTERCARD", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().debit().withAmount("10").withCard(Card.MASTERCARD_1).asThreeds().withResponseUrl()
                        .then().refund().withAmount("10").referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("REGISTER >> DEBIT >> partial REFUND", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().register().withCard(Card.MASTERCARD_1)
                        .then().debit().withAmount("20").referringToNth(TransactionCode.REGISTERATION).asThreeds().withResponseUrl()
                        .then().refund().withAmount("10").referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("PREAUTH >> CAPTURE >> REFUND", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().preauthorization().withAmount("100").withCard(Card.MASTERCARD_1).asThreeds().withResponseUrl()
                        .then().capture().withAmount("100").referringToNth(TransactionCode.PREAUTHORIZATION)
                        .then().refund().withAmount("100").referringToNth(TransactionCode.CAPTURE), "MASTER")
        );
    }

    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of("MASTERCARD", Card.MASTERCARD_1, Merchant.SIX_THREEDS_TWO_MERCHANT),
                Arguments.of("VISA", Card.VISA_8, Merchant.SIX_THREEDS_TWO_MERCHANT)
        );
    }

}
