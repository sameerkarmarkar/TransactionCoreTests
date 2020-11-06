package com.unzer.tests.threeds.creditcard.threeds;

import com.unzer.constants.*;
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

public class RecurringThreedsTransactions {

    @Test
    public void shouldKeepTheTransactionPendingWhenThreedsAuthorizationIsNotCompleted() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                .startWith().register().withCard(Card.MASTERCARD)
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
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                .startWith().register().withCard(Card.MASTERCARD)
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
    @MethodSource("merchants")
    public void shouldNotNeedThreedsAuthorizationForUnscheduledRepeatedRecurring(String description, Flow flow) {
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

    private static Stream<Arguments> merchants() {
        return Stream.of(
                Arguments.of("Threeds Version One flow", Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                        .startWith().register().withCard(Card.MASTERCARD)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                        .then().preauthorization().referringToNth(TransactionType.REGISTRATION)),
                Arguments.of("Threeds version two flow", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                        .startWith().register().withCard(Card.VISA)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).and().withResponseUrl().and().asThreeds()
                        .then().debit().referringToNth(TransactionType.REGISTRATION))
        );
    }
}
