package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Contact {
    @XmlElement(name = "Email")
    protected String email = "testmail@hpc-consulting.de";

    @XmlElement(name = "Ip")
    protected String ip = "12.12.12.12";
}
