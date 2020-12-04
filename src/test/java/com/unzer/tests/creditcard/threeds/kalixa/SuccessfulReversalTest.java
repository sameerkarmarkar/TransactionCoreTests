package com.unzer.tests.creditcard.threeds.kalixa;

import com.unzer.tests.BaseTest;
import com.unzer.util.Flow;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.unzer.constants.Card.MASTERCARD_1;
import static com.unzer.constants.PaymentMethod.CREDITCARD;
import static com.unzer.constants.Merchant.KALIXA_NON_THREED_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_ONE_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_TWO_MERCHANT;
import static com.unzer.constants.ThreedsVersion.*;
import static com.unzer.constants.TransactionCode.*;

public class SuccessfulReversalTest implements BaseTest {

    @Test
    public void shouldProcessReversalForNonThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
        .startWith().preauthorization().withAmount("10").withCard(MASTERCARD_1)
        .then().reversal().referringToNth(PREAUTHORIZATION).withAmount("10").execute();
    }

    @Test
    public void shouldProcessReversalForThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .then().reversal().referringToNth(PREAUTHORIZATION).withAmount("10").execute();
    }

    //TODO: Enable once the reversal is working for debit
    @Disabled
    @Test
    public void shouldProcessReversalForNonThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("10")
                .then().reversal().referringToNth(PREAUTHORIZATION).withAmount("10").execute();
    }

    //TODO: Enable once the reversal is working for debit
    @Disabled
    @Test
    public void shouldProcessReversalForThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withAmount("10").withCard(MASTERCARD_1)
                .and().withResponseUrl().asThreeds(VERSION_2)
                .then().reversal().referringToNth(DEBIT).withAmount("10").execute();
    }

    //TODO: Enable once the reversal is working for capture
    @Disabled
    @Test
    public void shouldProcessReversalForCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withAmount("10").withCard(MASTERCARD_1)
                .withResponseUrl().and().asThreeds(VERSION_2)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("5")
                .then().reversal().referringToNth(CAPTURE).withAmount("5")
                .execute();
    }

    //TODO: Enable once the reversal is working for rebill
    @Disabled
    @Test
    public void shouldProcessReversalForRebill() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withAmount("10").withCard(MASTERCARD_1)
                .withResponseUrl().and().asThreeds(VERSION_1)
                .then().rebill().referringToNth(DEBIT).withAmount("5")
                .then().reversal().referringToNth(REBILL).withAmount("5")
                .execute();

    }
}
