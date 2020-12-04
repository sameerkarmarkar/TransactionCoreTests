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

public class SuccessfulCaptureTest implements BaseTest {

    @Test
    public void shouldProcessFullCaptureForNonThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("5.29")
                .execute();
        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("5.29").execute();

    }

    @Test
    public void shouldProcessFullCaptureForThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .execute();

        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("10").execute();

    }

    @Test
    public void shouldProcessFullCaptureForCofPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();

        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.35").execute();

    }

    @Test
    public void shouldProcessMultiplePartialCapturesForNonThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("5.29")
                .execute();
        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("2.25").execute();

        //TODO:Enable after partial capture has been implemented for KALIXA
        //flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("2.00").execute();
    }

    @Test
    public void shouldProcessMultiplePartialCapturesForThreedsPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();

        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("5").execute();

        //TODO:Enable after partial capture has been implemented for KALIXA
        /*flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("3.25").execute();
        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.75").execute();*/

    }

    //TODO: Enable after partial capture has been implemented for KALIXA
    @Disabled
    @Test
    public void shouldProcessMultiplePartialCapturesForCofPreauth() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("5")
                .execute();

        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.25").execute();

        //TODO:Enable after partial capture has been implemented for KALIXA
        /*flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.25").execute();
        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.25").execute();
        flow.continueWith().capture().referringToNth(PREAUTHORIZATION).withAmount("1.25").execute();*/

    }


}
