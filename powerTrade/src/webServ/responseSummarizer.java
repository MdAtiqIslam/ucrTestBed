/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webServ;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

/**
 *
 * @author atiq
 */
public class responseSummarizer {

    private static int starRecordNo = 3;
    private static char starRecordId = 'N';
    private static int noOfRecords = 60;
    private static String fileLocation = "D:\\Dropbox\\PhD Research\\Papers\\20 Power Trade\\experiments\\webserv\\results\\response_different_speed\\OlioDriver.";
    private static HashMap<String, Double> avgResponse = new HashMap<>();
    private static HashMap<String, Double> p90Response = new HashMap<>();
    private static HashMap<String, Double> p99Response = new HashMap<>();
    private static HashMap<String, Double> eventMix = new HashMap<>();

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        char currentRecordID = starRecordId;
        int currentRecordNo = starRecordNo;

        for (int i = 0; i < noOfRecords; i++) {
            String file = fileLocation + currentRecordNo + currentRecordID + "\\summary.xml";
            //System.out.println(file);
            Document doc = getDoc(file);
            resultMixParser(doc);
            resultSummaryParser(doc);

            String[] events = {"HomePage", "Login", "TagSearch", "EventDetail", "PersonDetail", "AddPerson", "AddEvent"};
            double avgResponseWeighted = 0;
            for (String event : events) {
                avgResponseWeighted += avgResponse.get(event) * eventMix.get(event);
                //System.out.print(avgResponse.get(event));
            }
            double p90ResponseWeighted = 0;
            for (String event : events) {
                p90ResponseWeighted += p90Response.get(event) * eventMix.get(event);
                //System.out.print(avgResponse.get(event));
            }

            double p99ResponseWeighted = 0;
            for (String event : events) {
                p99ResponseWeighted += p90Response.get(event) * eventMix.get(event);
                //System.out.print(avgResponse.get(event));
            }
            System.out.println(currentRecordNo + "" + currentRecordID + ": Average response=" + avgResponseWeighted
                    + ", 90% response=" + p90ResponseWeighted
                    + ", 99% response=" + p99ResponseWeighted);

            if (currentRecordID != 'Z') {
                currentRecordID++;
            } else {
                currentRecordID = 'A';
                currentRecordNo++;
            }

        }

    }

    private static Document getDoc(String filePath) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(filePath);
        return doc;
    }

    public static void resultSummaryParser(Document doc) {

        NodeList benchmark = doc.getElementsByTagName("responseTimes");
        Node responseTimes = benchmark.item(0);
        NodeList responseOPerations = responseTimes.getChildNodes();
        for (int i = 0; i < responseOPerations.getLength(); i++) {
            if (responseOPerations.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element operation = (Element) responseOPerations.item(i);
                String name = operation.getAttribute("name");
                double avgRes = 0;
                double p90Res = 0;
                double p99Res = 0;
                //System.out.println("Operation Name:" + name);
                NodeList opList = operation.getChildNodes();
                for (int j = 0; j < opList.getLength(); j++) {
                    Node opResults = opList.item(j);
                    if (opResults.getNodeType() == Node.ELEMENT_NODE) {
                        Element opResultItem = (Element) opResults;
                        if (opResultItem.getNodeName().equals("avg")) {
                            avgRes = Double.parseDouble(opResultItem.getTextContent());
                            //System.out.print(name + "mix=" + mix+ "\\n");
                            avgResponse.put(name, avgRes);
                        } else if (opResultItem.getNodeName().equals("p90th")) {
                            p90Res = Double.parseDouble(opResultItem.getTextContent());
                            p90Response.put(name, p90Res);
                        } else if (opResultItem.getNodeName().equals("p99th")) {
                            p99Res = Double.parseDouble(opResultItem.getTextContent());
                            p99Response.put(name, p99Res);
                        }
                    }
                }
            }

        }
    }

    public static void resultMixParser(Document doc) {

        NodeList benchmark = doc.getElementsByTagName("mix");
        Node responseTimes = benchmark.item(0);
        NodeList responseOPerations = responseTimes.getChildNodes();
        for (int i = 0; i < responseOPerations.getLength(); i++) {
            if (responseOPerations.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element operation = (Element) responseOPerations.item(i);
                String name = operation.getAttribute("name");
                double mix = 0;
                //System.out.println("Operation Name:" + name);
                NodeList opList = operation.getChildNodes();
                for (int j = 0; j < opList.getLength(); j++) {
                    Node opResults = opList.item(j);
                    if (opResults.getNodeType() == Node.ELEMENT_NODE) {
                        Element opResultItem = (Element) opResults;
                        if (opResultItem.getNodeName().equals("mix")) {
                            mix = Double.parseDouble(opResultItem.getTextContent());
                            //System.out.print(name + "mix=" + mix+ "\\n");
                            eventMix.put(name, mix);
                        }
                    }
                }
            }

        }
    }

}
