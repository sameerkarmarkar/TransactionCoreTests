package com.unzer.constants;

import net.hpcsoft.adapter.payonxml.ProcessingType;
import net.hpcsoft.adapter.payonxml.ReasonType;
import net.hpcsoft.adapter.payonxml.ReturnType;
import net.hpcsoft.adapter.payonxml.StatusType;

import static com.unzer.chef.DataChef.statusType;
import static com.unzer.chef.DataChef.reasonType;
import static com.unzer.chef.DataChef.returnType;

public enum TransactionProcessing {
    SUCCESSFUL ("90", "ACK",
            statusType("90", "NEW"),
            reasonType("00","SUCCESSFULL"),
            returnType("000.100","Request successfully processed")),
    PENDING ("80", "ACK",
            statusType("80", "WAITING"),
            reasonType("00","Transaction Pending"),
            returnType("000.200.000","Transaction pending"))
    ;

    private ProcessingType processingType;

    TransactionProcessing(String code, String result, StatusType status, ReasonType reason, ReturnType returnType) {
        this.processingType = new ProcessingType();
        processingType.setCode(code);
        processingType.setResult(result);
        processingType.setStatus(status);
        processingType.setReason(reason);
        processingType.setReturn(returnType);
    }

    public ProcessingType getProcessingType() {
        return this.processingType;
    }

    @Override
    public String toString() {
        return "processing.code:"+this.getProcessingType().getCode() +"\n"
                + " processing.result:"+this.getProcessingType().getResult() +"\n"
                + " processing.status.code:"+this.getProcessingType().getStatus().getCode() +"\n"
                + " processing.status.value:"+this.getProcessingType().getStatus().getValue() +"\n"
                + " processing.reason.code:"+this.getProcessingType().getReason().getCode() +"\n"
                + " processing.reason.valu:"+this.getProcessingType().getReason().getValue() +"\n"
                + " processing.return.code:"+this.getProcessingType().getReturn().getCode() +"\n"
                + " processing.return.value:"+this.getProcessingType().getReturn().getValue();
    }

}
