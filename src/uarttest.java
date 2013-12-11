

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class uarttest {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			System.out.print(portList2());
			
	}
	public static String portList(String msg){
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while(ports.hasMoreElements()){
			CommPortIdentifier cpIdentifier = (CommPortIdentifier)ports.nextElement();
			msg = msg + cpIdentifier.getName();	
		}
		
		return msg;
	}
	public static String portList2(){
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		String msg ="";
		while(ports.hasMoreElements()){
			CommPortIdentifier cpIdentifier = (CommPortIdentifier)ports.nextElement();
			msg = msg + cpIdentifier.getName();	
		}		
		return msg;
	}
	public static List<String> portList3(){
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		List<String> pList = new ArrayList<String>();
		while(ports.hasMoreElements()){
			CommPortIdentifier cpIdentifier = (CommPortIdentifier)ports.nextElement();
			String tmp = new String();
			tmp = cpIdentifier.getName();
			pList.add(tmp);
		}		
		return pList;
	}
}
