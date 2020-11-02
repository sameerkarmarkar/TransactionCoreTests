package com.unzer.util;

import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;

import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class Formatter {
    private static final String CONTEXT_PATH = "net.hpcsoft.adapter.payonxml";
    public static String marshall(JAXBElement<RequestType> obj) throws JAXBException {
        Writer writer = new StringWriter();
        JAXBContext jc = JAXBContext
                    .newInstance(CONTEXT_PATH);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(obj, writer);
        return writer.toString();
    }

    public static final JAXBElement<ResponseType> unmarshal(String response) throws JAXBException {
        StringReader reader = new StringReader(response);
        JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (JAXBElement<ResponseType>) unmarshaller.unmarshal(reader);
    }
}
