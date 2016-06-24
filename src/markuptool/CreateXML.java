package markuptool;

/**
 *
 * @author Aslam
 */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CreateXML {

    private JFileChooser fc = new JFileChooser();
    private String filePathName;

    private DocumentBuilderFactory docFactory;
    private DocumentBuilder docBuilder;
    private Document xmlDoc;
    private Element wordElem; //word node
    private Element rootElem; //markup node
    private Element mainElem; //text node
    private Element unmarkedTextElem; //unmarked_text node
    private Element wordIndicatorElem;
    private Element flagIndicationElem;
    private Element correctSpellElem;
    private Element displayedSpellElem;
    private Element metadataElem;
    private Element titleElem;
    private Element descElem;
    private Element sourceElem;
    private File textFile;
    private BufferedReader bReader;
    private StringBuilder builder;
    private String unmarked = null;//string to represent text inside unmarked node
    boolean isRead = false;

    private String titleText;//xml file title
    private String fileDesc; //xml file description
    private String source;

    public CreateXML(File file, String title, String desc, String src) throws ParserConfigurationException, IOException,
            FileNotFoundException, SAXException, TransformerConfigurationException, TransformerException {

        textFile = file;
        titleText = title;
        fileDesc = desc;
        source = src;
        createFile();
    }

    public CreateXML() throws ParserConfigurationException, IOException,
            FileNotFoundException, SAXException, TransformerConfigurationException, TransformerException {
    }

    public void createFile() throws FileNotFoundException, IOException,
            ParserConfigurationException, SAXException, TransformerConfigurationException, TransformerException {

        createXMLFile();
        if (textFile != null) {
            JOptionPane.showMessageDialog(null, "please choose a name and location"
                    + " for your XML file.");
            checkAndCreate();
        }
        if (fc.getSelectedFile() != null) {
            filePathName += ".xml";
            File aFile = new File(filePathName);
            if (aFile.exists()) {
                //if the file with same name already exists, dont' duplicate it.
                JOptionPane.showMessageDialog(null, "File with the same name "
                        + "already exists, please choose another");
                //to clear text area;
                filePathName = null;
                fc.setSelectedFile(null);
                if (filePathName == null) {
                    GUI.textArea.setText(null);
                }

            } else {

                outputXML(xmlDoc);
            }
        }
    }

    public void createXMLFile() throws ParserConfigurationException,
            FileNotFoundException, IOException, SAXException, TransformerConfigurationException, TransformerException {

        if (textFile != null) {

            if (!isRead) {
                builder = new StringBuilder();

                bReader = new BufferedReader(new FileReader(textFile));

                //go through all the lines of the texts in the file and output it 
                //onto the xml file.
                try {
                    String line = bReader.readLine();
                    while (line != null) {
                        builder.append("\n");
                        builder.append(line);

                        builder.append("\n");

                        line = bReader.readLine();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (bReader != null) {
                        bReader.close();
                    }
                }

                isRead = true; //the file only needs to be read once.
            }

            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            xmlDoc = docBuilder.newDocument();
            //create the nodes of the xml document
            //root node
            rootElem = xmlDoc.createElement("markup");
            xmlDoc.appendChild(rootElem);
            //add metadata tag 
            metadataElem = xmlDoc.createElement("metadata");
            rootElem.appendChild(metadataElem);
            //add title tag as child to metadata
            titleElem = xmlDoc.createElement("title");
            metadataElem.appendChild(titleElem);
            Text titleText = xmlDoc.createTextNode(this.titleText);
            titleElem.appendChild(titleText);
            //create and add description tag
            descElem = xmlDoc.createElement("desc");
            metadataElem.appendChild(descElem);
            Text descText = xmlDoc.createTextNode(fileDesc);
            descElem.appendChild(descText);
            //create and add source tag
            sourceElem = xmlDoc.createElement("source");
            metadataElem.appendChild(sourceElem);
            Text sourceText = xmlDoc.createTextNode(source);
            sourceElem.appendChild(sourceText);
            //text node
            mainElem = xmlDoc.createElement("text");
            rootElem.appendChild(mainElem);
            addUnmarkedText(builder.toString());

        }
    }

    public void removeEmptyTags(Node node) throws ParserConfigurationException, SAXException, IOException {
        //http://stackoverflow.com/questions/12524727/remove-empty-nodes-from-a-xml-recursively
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            removeEmptyTags(list.item(i));
        }

        boolean emptyElement = node.getNodeType() == Node.ELEMENT_NODE
                && node.getChildNodes().getLength() == 0;
        boolean emptyText = node.getNodeType() == Node.TEXT_NODE
                && node.getNodeValue().trim().isEmpty();
        if (emptyElement || emptyText) {
            node.getParentNode().removeChild(node);
        }

    }

    public void cleanXML() throws SAXException, IOException, ParserConfigurationException, FileNotFoundException, TransformerException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        String unmarked = this.unmarked;

        //remove line breaks and white space between tags.
        unmarked = unmarked.replace("\n", "").replace("\r", "").replaceAll(">\\s*<", "><");

        Document document = builder.parse(new InputSource(new StringReader(unmarked)));
        removeEmptyTags(document);
        outputXML(document);

    }

    public void addChanged() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {

            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(
                    new StringReader(unmarked)));

            outputXML(doc);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //read the text between the <unmarked_text></unmarked_text> tag
    //this method checks if the file user has named exists and creates it accordinlgy.
    //if the file has not been given an name.
    public void checkAndCreate() {
        fc.showSaveDialog(null);
        if (fc.getSelectedFile() != null) {
            /*prevents from a file e.g. "example.xml.xml" from being saved if the
             * file was saved as "example.xml".
             */
            filePathName = fc.getSelectedFile().getAbsolutePath();
            if (filePathName.contains(".xml")) {
                String newFile = filePathName.replaceAll(".xml", "");
                filePathName = newFile;
            }
        }
        if (filePathName == null) {
            GUI.textArea.setText(null);
        }
    }

    public void addUnmarkedText(String string) {
        //System.out.println(string);
        wordIndicatorElem = xmlDoc.createElement("word_indicator");
        flagIndicationElem = xmlDoc.createElement("flag_indicator");    
        unmarkedTextElem = xmlDoc.createElement("unmarked_text");
        mainElem.appendChild(unmarkedTextElem);
        //text content
        Text text = xmlDoc.createTextNode(string);
        unmarkedTextElem.appendChild(text);

    }

    public void outputXML(Document doc) throws FileNotFoundException, IOException,
            SAXException, TransformerConfigurationException, TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);

        StreamResult streamResult = new StreamResult(new File(filePathName));
        transformer.transform(source, streamResult);

    }

    public String getUnmarkedTextContent() {

        return unmarkedTextElem.getTextContent();
    }

    public void setUnmarked(String unmarked) {
        this.unmarked = unmarked;
    }

    public String getFileName() {
        return filePathName;
    }

    public void setTextFile(File textFile) {
        this.textFile = textFile;
    }

    public void setFileName(String fileName) {
        this.filePathName = fileName;
    }

    public void setMainElem(Element mainElem) {
        this.mainElem = mainElem;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

}
