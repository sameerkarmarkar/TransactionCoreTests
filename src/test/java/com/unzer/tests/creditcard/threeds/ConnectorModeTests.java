package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.tests.BaseTest;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ConnectorModeTests extends BaseTest {

    private static final TransactionMode mode = TransactionMode.CONNECTOR_TEST;

    @Test
    public void shouldProcessThreedsTwoTransactionInConnectortestMode() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).inMode(mode)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().debit().referringToNth(TransactionCode.REGISTERATION).and().withResponseUrl().and().asThreeds()
                .then().debit().referringToNth(TransactionCode.REGISTERATION);

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
    }

    @Test
    public void shouldProcessScheduledThreedsTransaction() {
        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT).inMode(mode)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().preauthorization().referringToNth(TransactionCode.REGISTERATION).withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                .then().schedule().referringToNth(TransactionCode.REGISTERATION).withSchedule(TransactionCode.PREAUTHORIZATION);

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
                () -> assertThat("Scheduled transaction type was invalid", DatabaseHelper.getTransactionType(scheduledTransactionId), equalTo("RES")),
                () -> assertThat("Transaction is not recorded as scheduled transaction", DatabaseHelper.isScheduled(scheduledTransactionId)),
                () -> assertThat("Transaction is not recorded as MIT transaction", DatabaseHelper.getInitiation(scheduledTransactionId), equalTo("MIT")),
                () -> assertThat("Transaction is not recorded as SUBSEQUENT transaction", DatabaseHelper.getInitialSubsequent(scheduledTransactionId), equalTo("SUBSEQUENT"))
        );

    }
}
