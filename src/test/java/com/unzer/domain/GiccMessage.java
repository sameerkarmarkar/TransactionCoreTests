package com.unzer.domain;

import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(name = "isomsg")
@Data
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class GiccMessage {
    private List<GiccField> field;

    public GiccField fieldById(String id) {
        return field.stream().filter(f -> f.getId().equals(id)).findFirst().get();
    }
}
