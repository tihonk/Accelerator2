package com.accelerator.services.implementations;

import com.accelerator.services.XlsxService;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Service("xlsxService")
public class XlsxServiceImpl implements XlsxService {

    private static final String ROW_ELEMENT = "row";
    private static final String CELL_ELEMENT = "c";
    private static final String VALUE_ELEMENT = "v";

    private XMLEventFactory eventFactory;
    private SharedStringsTable sharedstringstable;
    private XMLEventWriter writer;

    @Override
    public XMLEventWriter writeValuesInNewRows(XMLEventFactory eventFactory, SharedStringsTable sharedstringstable,
                                               XMLEventWriter writer, List<String> values) throws XMLStreamException {
        declarePrivateFields(eventFactory, sharedstringstable, writer);
        fillNewRows(values);
        return this.writer;
    }

    @Override
    public XMLEventWriter writeValueInNewCell(XMLEventFactory eventFactory, SharedStringsTable sharedstringstable,
                                              XMLEventWriter writer, String value) throws XMLStreamException {
        declarePrivateFields(eventFactory, sharedstringstable, writer);
        fillNewCell(value);
        return this.writer;
    }

    @Override
    public XMLEvent prepareFillingValueEvent(XMLEventFactory eventFactory,
                                             SharedStringsTable sharedstringstable, String value) {
        declarePrivateFields(eventFactory, sharedstringstable, null);
        return prepareValueToFill(value);
    }


    private void declarePrivateFields(XMLEventFactory eventFactory, SharedStringsTable sharedstringstable, XMLEventWriter writer) {
        this.eventFactory = eventFactory;
        this.sharedstringstable = sharedstringstable;
        this.writer = writer;
    }

    private void fillNewRows(List<String> values) throws XMLStreamException {
        for (String value : values) {
            writer.add(newStartElement(ROW_ELEMENT, null));
            fillNewCell(value);
            writer.add(newEndElement(ROW_ELEMENT));
        }
    }

    private void fillNewCell(String value) throws XMLStreamException {
        writer.add(newStartElement(CELL_ELEMENT, getAttributeIterator()));
        writer.add(newStartElement(VALUE_ELEMENT, null));
        writer.add(prepareValueToFill(value));
        writer.add(newEndElement(VALUE_ELEMENT));
        writer.add(newEndElement(CELL_ELEMENT));
    }

    private StartElement newStartElement(String elementName, Iterator iterator) {
        return eventFactory.createStartElement(new QName(elementName), iterator, null);
    }

    private EndElement newEndElement(String elementName) {
        return eventFactory.createEndElement(new QName(elementName), null);
    }

    private XMLEvent prepareValueToFill(String value) {
        CTRst ctrst = CTRst.Factory.newInstance();
        ctrst.setT(value);
        int sRef = sharedstringstable.addEntry(ctrst);
        return eventFactory.createCharacters(Integer.toString(sRef));
    }

    private Iterator getAttributeIterator() {
        Attribute attribute = eventFactory.createAttribute("t", "s");
        List attributeList = Collections.singletonList(attribute);
        return attributeList.iterator();
    }
}
