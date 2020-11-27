package com.unzer.verifiers;

import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;

public class ResponseVerifier {

    private Flow flow;
    private ResponseType transactionResponse;
    private RequestType transactionRequest;

    public void verifyResponse(Flow flow) {
        this.flow = flow;
        this.transactionResponse = flow.getLastTransactionResponse();
        this.transactionRequest = flow.getLastTransactionRequest();
    }

}
