package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.tests.BaseTest;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import com.unzer.util.GiccVerifier;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GiccProtocolTest extends BaseTest {

    private static final GiccVerifier GICC_VERIFIER = GiccVerifier.INSTANCE;

    @ParameterizedTest(name = "{0}")
    @MethodSource("oneOffTransactions")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForOneOffThreedsTwoTransaction(String description, Flow flow) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        RequestType request = flow.getLastTransactionRequest();
        String shortId = response.getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(request.getTransaction().getAccount().getBrand());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("returningCustomer")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForReturningCustomerThreedsTwoTransaction(String description, Flow flow, String cardBrand) {
        flow.execute();
        RequestType request = flow.getLastTransactionRequest();
        ResponseType response = flow.getLastTransactionResponse();
        ResponseType parentResponse = flow.getParentResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        String parentShortId = parentResponse.getTransaction().getIdentification().getShortID();
        assertThat("Transaction was not successful", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL"));
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForReturningCustomer(cardBrand, parentShortId);
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledInitialRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds();
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertThat("Transaction was not successful", response.getTransaction().getProcessing().getReason().getValue(), equalTo("Transaction Pending"));
        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledInitialRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledRepeatedRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds()
                .then().debit().withResponseUrl().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.REPEATED);
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledSubsequentRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForSubsequentScheduledThreedsTwoTransaction(Card card, Merchant merchant) {

        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(card)
                .then().debit().referringToNth(TransactionCode.REGISTERATION).and().withRecurringIndicator(Recurrence.INITIAL).withResponseUrl().and().asThreeds()
                .then().schedule().withSchedule(TransactionCode.DEBIT).referringToNth(TransactionCode.REGISTERATION);
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        String scheduledTransaction  = DatabaseHelper.getScheduledTransactionShortId(shortID);
        GICC_VERIFIER.withShortId(scheduledTransaction).getMessage().and().verifyFieldsForScheduledSubsequentRecurring(card.getCardBrand());
    }

    private static Stream<Arguments> oneOffTransactions() {
        return Stream.of(
                Arguments.of("One off preauth with Mastercard",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().preauthorization().withCard(Card.MASTERCARD_5).asThreeds().withResponseUrl()),
                Arguments.of("One off preauth with Visa",
                        Flow.forMerchant(Merchant.POSTBANK_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                                .startWith().debit().withCard(Card.VISA_3).asThreeds().withResponseUrl())
        );
    }

    private static Stream<Arguments> returningCustomer() {
        return Stream.of(
                Arguments.of("DEBIT >> REFUND with MASTERCARD", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().debit().withCard(Card.MASTERCARD_2).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("REGISTER >> DEBIT >> REFUND with VISA", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().register().withCard(Card.MASTERCARD_1)
                        .then().debit().referringToNth(TransactionCode.REGISTERATION).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionCode.DEBIT), "MASTER"),
                Arguments.of("PREAUTH >> CAPTURE >> REFUND with VISA", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).withPaymentMethod(PaymentMethod.CREDITCARD)
                        .startWith().preauthorization().withCard(Card.MASTERCARD_1).asThreeds().withResponseUrl()
                        .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION)
                        .then().refund().referringToNth(TransactionCode.CAPTURE), "MASTER")
        );
    }

    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.MASTERCARD_1, Merchant.SIX_THREEDS_TWO_MERCHANT),
                Arguments.of(Card.VISA_1, Merchant.SIX_THREEDS_TWO_MERCHANT)
        );
    }

}
