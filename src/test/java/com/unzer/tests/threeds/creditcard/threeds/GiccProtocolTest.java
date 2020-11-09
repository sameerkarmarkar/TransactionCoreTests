package com.unzer.tests.threeds.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import com.unzer.util.GiccVerifier;
import com.unzer.util.RequestBuilder;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.unzer.constants.Constants.DESTINATION;
import static com.unzer.util.Formatter.marshall;
import static com.unzer.util.Formatter.unmarshal;

public class GiccProtocolTest {

    private static final GiccVerifier GICC_VERIFIER = GiccVerifier.INSTANCE;
    private static final QName _Request_QNAME = new QName("", "Request");

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
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForReturningCustomer(cardBrand, parentShortId);
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledInitialRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds();
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledInitialRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledRepeatedRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds()
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.REPEATED);
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledSubsequentRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForSubsequentScheduledThreedsTwoTransaction(Card card, Merchant merchant) {

        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).withResponseUrl().and().asThreeds()
                .then().schedule().withSchedule(TransactionType.DEBIT).referringToNth(TransactionType.REGISTRATION);
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        String scheduledTransaction  = DatabaseHelper.getScheduledTransactionShortId(shortID);
        GICC_VERIFIER.withShortId(scheduledTransaction).getMessage().and().verifyFieldsForScheduledSubsequentRecurring(card.getCardBrand());
    }

    private static Stream<Arguments> oneOffTransactions() {
        return Stream.of(
                Arguments.of("One off preauth with Mstercard",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().preauthorization().withCard(Card.MASTERCARD).asThreeds().withResponseUrl()),
                Arguments.of("One off preauth with Visa",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().debit().withCard(Card.MASTERCARD).asThreeds().withResponseUrl())
        );
    }

    private static Stream<Arguments> returningCustomer() {
        return Stream.of(
                Arguments.of("DEBIT >> REFUND with MASTERCARD", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().debit().withCard(Card.MASTERCARD).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionType.DEBIT), "MASTER"),
                Arguments.of("REGISTER >> DEBIT >> REFUND with VISA", Flow.forMerchant(Merchant.POSTBANK_THREEDS_TWO_MERCHANT).startWith().register().withCard(Card.VISA)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionType.DEBIT), "VISA")
        );
    }

    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.MASTERCARD, Merchant.SIX_THREEDS_TWO_MERCHANT),
                Arguments.of(Card.VISA, Merchant.SIX_THREEDS_TWO_MERCHANT)
        );
    }

}
