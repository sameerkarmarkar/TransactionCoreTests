package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.tests.BaseTest;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class RecurringThreedsTransactions extends BaseTest {

    private static final TestMode mode = TestMode.INTEGRATOR_TEST;

    @Test
    public void shouldKeepTheTransactionPendingWhenThreedsAuthorizationIsNotCompleted() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT).inMode(mode)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().debit().referringToNth(TransactionType.REGISTRATION).withResponseUrl();

        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertAll(
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getCode(), equalTo("80")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getValue(), equalTo("WAITING")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () ->assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("Transaction Pending")),
                () ->assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(response.getTransaction().getIdentification().getShortID()), equalTo("80"))
        );

    }

    @Test
    public void shouldCompleteTransactionProcesingWhenThreedsAuthorizationIsCompleted() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT).inMode(mode)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().preauthorization().referringToNth(TransactionType.REGISTRATION).withResponseUrl().asThreeds(ThreedsVersion.VERSION_1);

        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertAll(
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getCode(), equalTo("80")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getValue(), equalTo("WAITING")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () ->assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("Transaction Pending")),
                () ->assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(response.getTransaction().getIdentification().getShortID()), equalTo("90"))
        );

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unscheduledRecurringFlows")
    public void shouldNotNeedThreedsAuthorizationForUnscheduledRepeatedRecurring(String description, Flow flow) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertAll(
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getCode(), equalTo("90")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getValue(), equalTo("NEW")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () ->assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () ->assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () ->assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(response.getTransaction().getIdentification().getShortID()), equalTo("90"))
        );

    }

    //@ParameterizedTest(name = "{0}")
    //@MethodSource("scheduledRecurringFlows")
    public void shouldNotNeedThreedsAuthorizationForScheduledRepeatedRecurring(String description, Flow flow, String scheduledTxnType) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();

        assertAll(
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getCode(), equalTo("90")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getValue(), equalTo("NEW")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () -> assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(response.getTransaction().getIdentification().getShortID()), equalTo("90"))
        );

        String scheduledTransactionId = DatabaseHelper.getScheduledTransactionShortId(response.getTransaction().getIdentification().getShortID());
        assertAll(
                () -> assertThat("Scheduled transaction was unsuccessful", DatabaseHelper.getTransactionStatus(scheduledTransactionId), equalTo("90")),
                () -> assertThat("Scheduled transaction type was invalid", DatabaseHelper.getTransactionType(scheduledTransactionId), equalTo(scheduledTxnType)),
                () -> assertThat("Transaction is not recorded as scheduled transaction", DatabaseHelper.isScheduled(scheduledTransactionId)),
                () -> assertThat("Transaction is not recorded as MIT transaction", DatabaseHelper.getInitiation(scheduledTransactionId), equalTo("MIT")),
                () -> assertThat("Transaction is not recorded as SUBSEQUENT transaction", DatabaseHelper.getInitialSubsequent(scheduledTransactionId), equalTo("SUBSEQUENT"))
        );

    }

    @Test
    public void shouldNotAllowThreedsTransactionInScheduleWithoutInitialAutorization() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).inMode(mode)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().schedule().withSchedule(TransactionType.DEBIT).referringToNth(TransactionType.REGISTRATION);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertAll(
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getCode(), equalTo("90")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getStatus().getValue(), equalTo("NEW")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () -> assertThat("Invalid transaction status", response.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () -> assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(response.getTransaction().getIdentification().getShortID()), equalTo("90"))
        );

        String scheduledTransactionId = DatabaseHelper.getScheduledTransactionShortId(response.getTransaction().getIdentification().getShortID());
        assertThat("Scheduled transaction was unsuccessful", DatabaseHelper.getTransactionStatus(scheduledTransactionId), equalTo("70"));
    }

    private static Stream<Arguments> unscheduledRecurringFlows() {
        return Stream.of(
                Arguments.of("Threeds Version One flow: REG >> PREAUTH >> PREAUTH (SIX)",
                        Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_1)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION)
                        .and().withResponseUrl().asThreeds(ThreedsVersion.VERSION_1).withRecurringIndicator(Recurrence.INITIAL)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION).withRecurringIndicator(Recurrence.REPEATED)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT (EVO)",
                        Flow.forMerchant(Merchant.EVO_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.VISA_2)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT >> DEBIT (KALIXA)",
                        Flow.forMerchant(Merchant.KALIXA_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_3)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().debit().referringToNth(TransactionType.REGISTRATION)
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version one flow: REG >> PREAUTH >> DEBIT >> DEBIT (KALIXA)",
                        Flow.forMerchant(Merchant.KALIXA_THREEDS_ONE_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_2)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                        .then().debit().referringToNth(TransactionType.REGISTRATION)
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT >> DEBIT (PAYONE)",
                        Flow.forMerchant(Merchant.PAYONE_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_3)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().debit().referringToNth(TransactionType.REGISTRATION)
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT (POSTBANK)",
                        Flow.forMerchant(Merchant.POSTBANK_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_3)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT (POSTBANK)",
                        Flow.forMerchant(Merchant.POSTBANK_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_3)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().refund().referringToNth(TransactionType.DEBIT)
                        .then().debit().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> DEBIT (POSTBANK)",
                        Flow.forMerchant(Merchant.POSTBANK_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD_3)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().capture().referringToNth(TransactionType.PREAUTHORIZATION)
                        .then().debit().referringToNth(TransactionType.REGISTRATION))

                );
    }

    private static Stream<Arguments> scheduledRecurringFlows() {
        return Stream.of(
                Arguments.of("Threeds Version One flow: REG >> PREAUTH >> SCHEDULE (PREAUTH)",
                        Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                                .startWith().register().withCard(Card.MASTERCARD_1)
                                .then().preauthorization().referringToNth(TransactionType.REGISTRATION)
                                .and().withResponseUrl().asThreeds(ThreedsVersion.VERSION_1).withRecurringIndicator(Recurrence.INITIAL)
                                .then().schedule().withSchedule(TransactionType.PREAUTHORIZATION).referringToNth(TransactionType.REGISTRATION), "RES"),
                Arguments.of("Threeds version two flow: REG >> DEBIT >> SCHEDULE (DEBIT)",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                                .startWith().register().withCard(Card.MASTERCARD_1)
                                .then().debit().referringToNth(TransactionType.REGISTRATION)
                                .and().withResponseUrl().and().asThreeds()
                                .then().schedule().withSchedule(TransactionType.DEBIT).referringToNth(TransactionType.REGISTRATION), "DEB")
        );
    }


}