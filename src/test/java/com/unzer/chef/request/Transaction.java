package com.unzer.chef.request;

import com.unzer.constants.TestMode;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import static com.unzer.constants.Constants.*;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Transaction {
    @XmlAttribute(name = "mode")
    protected String mode = TestMode.CONNECTOR_TEST.name();

    @XmlAttribute(name = "response")
    protected String response = "SYNC";

    @XmlAttribute(name = "channel")
    protected String channel = CHANNEL;

    @XmlElement(name = "User")
    protected User user = new User();

    @XmlElement(name = "Identification")
    protected Identification identification = new Identification();

    @XmlElement(name = "Payment")
    protected Payment payment = new Payment();

    @XmlElement(name = "Account")
    protected Account account = new Account();

    @XmlElement(name = "Customer")
    protected Customer customer = new Customer();
}
