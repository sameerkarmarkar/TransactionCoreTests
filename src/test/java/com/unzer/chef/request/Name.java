package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Name {
    @XmlElement(name = "Given")
    protected String given = "Sameer";

    @XmlElement(name = "Family")
    protected String family = "Karmarkar";
}
