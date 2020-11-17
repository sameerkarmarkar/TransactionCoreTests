package com.unzer.tests.ppro;

import com.unzer.constants.Merchant;
import com.unzer.constants.TransactionCode;
import com.unzer.tests.BaseTest;
import com.unzer.util.Flow;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HPCTXNCORE-1934")
public class PproRefundTest extends BaseTest {

    @Test
    public void shouldSendUsageAsDynamicDescriptorInRefund() {
        Flow flow = Flow.forMerchant(Merchant.PPRO_IDEAL_MERCHANT)
                .startWith().preauthorization()
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION)
                .then().refund().referringToNth(TransactionCode.CAPTURE);

        flow.execute();
    }
}
