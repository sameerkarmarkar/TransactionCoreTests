package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Identification {
    @XmlElement(name = "TransactionID")
    protected String transctionId = UUID.randomUUID().toString();
}
