package gameTools.playerAnimations;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PlayerAnimationModel {

	// XML tags
	public final static String ANIMATION_TAG  = "Animation";
	public final static String ANIMATION_SEQUENCE_TAG  = "AnimationSequence";

	// private vars

	private ArrayList<PlayerSpriteModel> _animationSequence;
	private String _description;

	public PlayerAnimationModel() {
		_animationSequence = new ArrayList<PlayerSpriteModel>();
		_description = "Description";
	}

	public void addNewAnimation() {
		PlayerSpriteModel sprite = new PlayerSpriteModel(); 
		_animationSequence.add(sprite);
	}

	public void saveToXMLFile(File theFile) {
		saveToXMLFile(theFile, true, true);
	}
	public void saveToXMLFile(File theFile, boolean stdoutMode, boolean fileMode) {
		try {			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement(ANIMATION_TAG);
			doc.appendChild(rootElement);
			rootElement.appendChild(doc.createTextNode(_description));

			// animation elements

			Iterator<PlayerSpriteModel> iter = _animationSequence.iterator();
			while(iter.hasNext()) {
				PlayerSpriteModel spr = iter.next();
				Element seq = doc.createElement(ANIMATION_SEQUENCE_TAG);
				spr.convertToXML(doc, seq);
				rootElement.appendChild(seq);
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			if(stdoutMode) {
				StreamResult res = new StreamResult(System.out);				 
				transformer.transform(source, res);
			}
			if(fileMode) {
				StreamResult result = new StreamResult(theFile);
				transformer.transform(source, result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadFromXMLFile(File fXmlFile) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName(ANIMATION_SEQUENCE_TAG);

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					PlayerSpriteModel sprite = new PlayerSpriteModel();

					if(sprite.extractFromXML(eElement)) {
						_animationSequence.add(sprite);
					}

				}
			}
			saveToXMLFile(null, true, false); // log to stdout
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
