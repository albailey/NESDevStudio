package gameTools.playerAnimations;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PlayerSpriteModel {

	public PlayerSpriteModel() {
		
	}
  
	public void convertToXML(Document doc, Element element) {
		// set attribute to staff element
		Attr attr = doc.createAttribute("id");
		attr.setValue("1");
		element.setAttributeNode(attr);

	// salary elements
//	Element salary = doc.createElement("salary");
//	salary.appendChild(doc.createTextNode("100000"));
//	staff.appendChild(salary);
	}


    public  boolean extractFromXML(Element xmlElement){
  //  	  System.out.println("First Name : " + getTagValue("firstname", xmlElement));
  //  	    System.out.println("Last Name : " + getTagValue("lastname", xmlElement));
  //  	        System.out.println("Nick Name : " + getTagValue("nickname", xmlElement));
  //  	    System.out.println("Salary : " + getTagValue("salary", xmlElement));	
    	  
    	    return true;
    }
private static String getTagValue(String sTag, Element eElement) {
	NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
        Node nValue = (Node) nlList.item(0);
 
	return nValue.getNodeValue();
  }

}
