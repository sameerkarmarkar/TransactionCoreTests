package com.unzer.chef.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Payment {
    @XmlAttribute(name = "code")
    protected String code = "CC.PA";

    @XmlElement(name = "Presentation")
    protected Presentation presentation = Presentation.INSTANCE;
}
