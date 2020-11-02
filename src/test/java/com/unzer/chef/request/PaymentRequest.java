package com.unzer.chef.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Request")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentRequest {
    @XmlAttribute(name = "version")
    protected String version = "1.0";

    @XmlElement(name = "Header")
    protected Header header = new Header();

    @XmlElement(name = "Transaction")
    protected Transaction transaction = new Transaction();
}
