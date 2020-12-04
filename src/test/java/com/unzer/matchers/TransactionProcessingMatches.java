package com.unzer.matchers;

import com.unzer.constants.TransactionProcessing;
import net.hpcsoft.adapter.payonxml.ProcessingType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class TransactionProcessingMatches extends TypeSafeMatcher<ProcessingType> {

    private final TransactionProcessing transactionProcessing;

    private TransactionProcessingMatches(TransactionProcessing status) {
        this.transactionProcessing = status;
    }

    @Override
    protected boolean matchesSafely(ProcessingType processingType) {
        return (processingType.getCode().contains(transactionProcessing.getProcessingType().getCode())
                && processingType.getResult().equals(transactionProcessing.getProcessingType().getResult())
                && processingType.getStatus().getCode().equals(transactionProcessing.getProcessingType().getStatus().getCode())
                && processingType.getStatus().getValue().equals(transactionProcessing.getProcessingType().getStatus().getValue())
                && processingType.getReason().getCode().equals(transactionProcessing.getProcessingType().getReason().getCode())
                && processingType.getReason().getValue().equals(transactionProcessing.getProcessingType().getReason().getValue())
                && processingType.getReturn().getCode().contains(transactionProcessing.getProcessingType().getReturn().getCode())
                && processingType.getReturn().getValue().contains(transactionProcessing.getProcessingType().getReturn().getValue()));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("\n" + transactionProcessing.toString());
    }

    @Override
    public void describeMismatchSafely(ProcessingType processingType, Description description) {
        description.appendText("was \n")
                .appendText("processing.code:"+processingType.getCode()+"\n")
                .appendText("processing.result:"+processingType.getResult()+"\n")
                .appendText("processing.status.code:"+processingType.getStatus().getCode()+"\n")
                .appendText("processing.status.value:"+processingType.getStatus().getValue()+"\n")
                .appendText("processing.reason.code:"+processingType.getReason().getCode()+"\n")
                .appendText("processing.reason.value:"+processingType.getReason().getValue()+"\n")
                .appendText("processing.return.code:"+processingType.getReturn().getCode()+"\n")
                .appendText("processing.return.value:"+processingType.getReturn().getValue());
    }


    public static TransactionProcessingMatches transactionProcessingMatches(TransactionProcessing status) {
        return new TransactionProcessingMatches(status);
    }
}
