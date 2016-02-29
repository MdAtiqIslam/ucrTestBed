/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Test_1 {

    public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException, TransformerException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse("c:/summary.xml");
//        resultSummaryParser(doc);
        Document doc = builder.parse("c:/run.xml");
        NodeList configItem = doc.getElementsByTagName("fa:scale");
        Node scale = configItem.item(0);
        scale.setTextContent("100");
        for (int i=0; i<configItem.getLength();i++){
            Node item = configItem.item(i);
            System.out.println("Item:"+ i+ "Name:" + item.getNodeName()+" Node value:"+item.getNodeValue()+ "Node content"+item.getTextContent());           
        }
        
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new File("C:\\local_files\\testBed\\file_transfer\\web-serving\\automation\\output.xml"));
        Source input = new DOMSource(doc);

        transformer.transform(input, output);

    }

    public static void resultSummaryParser(Document doc) {

        NodeList benchmark = doc.getElementsByTagName("responseTimes");
        Node delayTimes = benchmark.item(0);
        NodeList responseOPerations = delayTimes.getChildNodes();
        for (int i = 0; i < responseOPerations.getLength(); i++) {
            if (responseOPerations.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element operation = (Element) responseOPerations.item(i);
                String name = operation.getAttribute("name");
                System.out.println("Operation Name:" + name);
                NodeList opList = operation.getChildNodes();
                for (int j = 0; j < opList.getLength(); j++) {
                    Node opResults = opList.item(j);
                    if (opResults.getNodeType() == Node.ELEMENT_NODE) {
                        Element opResultItem = (Element) opResults;
                        System.out.print(opResultItem.getNodeName() + "=" + opResultItem.getTextContent() + ", ");
                    }
                }
            }

        }
    }

}
