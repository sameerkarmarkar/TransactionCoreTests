package com.unzer.tests.ppro;

import com.unzer.constants.Merchant;
import com.unzer.tests.BaseTest;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static com.unzer.constants.PaymentMethod.ONLINE_TRANSFER;
import static com.unzer.constants.TransactionCode.*;

@Tag("HPCTXNCORE-1934")
@Slf4j
public class PproRefundTest extends BaseTest {

    @Test
    public void shouldSendUsageAsDynamicDescriptorInRefund() {
        Flow flow = Flow.forMerchant(Merchant.PPRO_IDEAL_MERCHANT).withPaymentMethod(ONLINE_TRANSFER)
                .startWith().preauthorization().withAccountHolder("Test Account", "ALIPAY");

        flow.execute();

        String receiptUniqueId = DatabaseHelper.getImplicitTransactionUniqueId(flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID(), "REC");
        flow.continueWith().refund().referringToTransactionWithId(receiptUniqueId);

        flow.execute();
    }
}
