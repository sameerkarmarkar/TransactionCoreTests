package com.unzer.chef.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Presentation {
    @XmlElement(name = "Amount")
    protected String amount = "121.45";

    @XmlElement(name = "Currency")
    protected String currency = "CHF";

    @XmlElement(name = "Usage")
    protected String usage = "SIX_VISA_1-DEB_CC.PA_TC04.2";

    protected static Presentation INSTANCE = new Presentation();
}
