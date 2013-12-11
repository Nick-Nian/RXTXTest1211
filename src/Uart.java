import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Uart {

	private CommPortIdentifier portIdentifier = null;
	private InputStream in = null;
	private OutputStream out = null;
	private SerialPort serialPort = null;
	
	public static void PortList()
	{
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier cpIdentifier = (CommPortIdentifier)ports.nextElement();
			System.out.println(cpIdentifier.getName());
		}
	}
	
	public void Open(String portName)
	{
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

			if (portIdentifier.isCurrentlyOwned())
			{
				System.out.println("Port in use!");
			}
			else
			{
				// points who owns the port and connection timeout
				serialPort = (SerialPort) portIdentifier.open("UART", 2000);

				// setup connection parameters
				//serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				// setup serial port reader and writer
				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();
				
				System.out.println("RXTX start");
				System.out.println("---------------------------------------------------------------");
			}
		} 
		catch (NoSuchPortException e)
		{
			e.printStackTrace();
		} 
		catch (PortInUseException e)
		{
			e.printStackTrace();
		} 
		catch (UnsupportedCommOperationException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void Send(byte[] bHex)
	{ 
       try {
            out.write(bHex);
            out.flush();
        }
       	catch (IOException e)
        {
          e.printStackTrace(); 
        } 	
    }
	
	public void Send(String sHex)
	{
		try {
			//out.write(Fun.HexToDecByte(sHex));
			out.write(Fun.StringToDecByte(sHex));
            out.flush();
            //System.out.println("UART-Tx: "+Fun.PrintHex(sHex));
            System.out.println("UART-Tx: "+ sHex);
		}
		catch (IOException e)
		{
			e.printStackTrace();
	    }
	}
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
	public String Recv()
	{
		String sHex = "";
		String rString = "";
		String urlString ="";
		String dataTypeString = "";
		String IDDString =""; //NEW
		String IDRString =""; //NEW
		String node_type_string=""; //NEW
		String sensing_type_string=""; //NEW

		try {
            int[] code = new int[256];
			int[] endcode = new int[2];
            int bits = 0;
            int urllen = 0;
            char [] url = new char[256];
            int[] IEEEAddr = new int[8];
            int[] shortAddr = new int[2];
            char[] dataType = new char[5];
            int[] lightValueBuf = new int[3];
            int[] tempValueBuf = new int[4];
            double lightValue=0;
            double tempValue=0;
            int IDDlen = 0; //NEW
            int IDRlen = 0; //NEW
            int node_type_len = 0; //NEW
            int sensing_type_len = 0; //NEW
            char [] IDD = new char[256];  //NEW
            char [] node_type = new char[256]; //NEW
            char [] IDR = new char[256]; //NEW
            char [] sensing_type = new char[256]; //NEW
            int theValue;
            int mask =(int)(Math.pow(2,16)-1);
            mask = mask>>4<<4;
            double double_value;
            double delta_EMF=0;
            double ppm = 0;
          
            
            
            // if stream is not bound in.read() method returns -1 in Windows, but it does not return -1 in Ubuntu
            while((code[bits] = in.read()) != -1)
            //while((code[bits] = in.read()) == 89)
			{
            	
            	//System.out.println(bits + " " + code[bits]); //for testing; 把所有接到的封包顯示出來 ??啁?撠?靘???箔?
            	bits++;
				if (bits > 2 && code[bits-2] == 170 && code[bits-1] == 254) break;
				if (bits == 16 && code[0] == 68){   //bit?? //ASCII 68=D ; Data bits?貊?桀??喳???yte??
					break;
				}else if (bits == 28 && code[0] == 82){ //ASCII 82=R ; Register bits?貊?桀??喳???yte??
					break;
				}else if (bits == 25 && code[0] == 83){ //ASCII 83=S ; Sensing rate
					break;
				}else if (bits == 2 && (code[0] == 255 || code[0] == 98 || code[0] == 0) ){ //ASCII 255 or 98代表是PIR的資料進來了
					break;
				}else if (bits == 2 && (code[0] >= 8 && code[0] <= 32)){ //ASCII 8 or 9 代表是CO2的資料進來了
				    break;
			    }
            }
            //嚙緘嚙踝蕭Xurl嚙踝蕭嚙?
            
            /*----------------------------------------*/
            //Here we deal with the CO2 message
            /*----------------------------------------*/
            if (code[0] >= 8 && code[0] <= 32)
            {
            	
            	theValue = (code[0] << 8) + code[1] ;
            	//System.out.println(theValue);
            	theValue = (theValue & mask)>>4;
            	double_value =(double)theValue;
            	double_value = ((double_value*3.3)/2047)*1000;
                delta_EMF = double_value - 217.635559;
                if(delta_EMF<0) delta_EMF = 0;
                ppm = Math.pow(10, ((delta_EMF+158.631)/62.877)) ;
                //System.out.println(ppm);
            	
            		rString = "CO2"+" "+ppm;
            	
            	
            
            }
            
            
            /*----------------------------------------*/
            //Here we deal with the PIR message
            /*----------------------------------------*/
            if (code[0] == 98 || code[0] == 255)
            {
            	if(code[0] == 98)
            	{
            		rString = "PIR" + " " + "YY";	 //感測到
            	}else if(code[0] == 255){
            		rString = "PIR" + " " + "NN";    //沒有感測到
            	}
            }
             
            
            /*----------------------------------------*/
            //Here we deal with the DF message
            /*----------------------------------------*/
            
            if (code[0] == 'R' && code[1] == 'e')
            {	
            //嚙賤收嚙趣的嚙踝蕭T嚙踝蕭INT 嚙賞成 BYTE
            //original i = 0
            	for (int i=0; i<bits; i++)
                {
            	 node_type[i] = (char) (code[i+9]);
             	 if (node_type[i] == 32) {
             		node_type_len = i; //i=8
             	    break;
             	 }
                }
               for (int i=0; i<bits; i++)
               {
            	 IDR[i] = (char) (code[i+9+node_type_len+1]);
            	 if (IDR[i] == 32) {
            	    IDRlen = i;
            	    break;
            	 }
               }
               for (int i=0; i<bits; i++)
               {
            	 sensing_type[i] = (char) (code[i+9+node_type_len+1+IDRlen+1]);
            	 if (sensing_type[i] == 32) {
            		 sensing_type_len = i;
            	    break;
            	 }
               }
               
               if (bits != 0)
               {
               	// the size of url[] is fixed to 256, so we have to create an array url2 whose length equal to 'urllen'.
            	   // in this way, we can use String.valueOf() to turn the char array into String...
            	   char [] IDR2 = new char[IDRlen];
                   for (int i=0; i<IDRlen; i++)
                    	IDR2[i] = IDR[i];
            	   IDRString = String.valueOf(IDR2);
            	   
            	   char [] node_type2 = new char[node_type_len];
                   for (int i=0; i<node_type_len; i++)
                	   node_type2[i] = node_type[i];
                   node_type_string = String.valueOf(node_type2);
                   
                   char [] sensing_type2 = new char[sensing_type_len];
                   for (int i=0; i<sensing_type_len; i++)
                	   sensing_type2[i] = sensing_type[i];
                   sensing_type_string = String.valueOf(sensing_type2);
            	  
            	//sHex = Fun.DecToHex(code, bits);       	
                   //rString = "DF " + urlString +" "+Fun.DecToHex(IEEEAddr, 8) +" "+ Fun.DecToHex(shortAddr, 2) ; 
            	   rString = "Register " + IDRString +" "+ node_type_string +" "+ sensing_type_string ;
            	   //System.out.println("in Uart:"+rString);
                //System.out.println("urlString length ="+ urlString.length());
            	   bits = 0;
               }
            }    
            else if (code[0] == 'D' && code[1] == 'a') {
            	//System.out.println("Data");
            	 /*----------------------------------------*/
                //Here we deal with the OB message
                /*----------------------------------------*/
            	for (int i=0; i<bits; i++)
                {
            	 IDD[i] = (char) (code[5+i]);
             	 //url[i] = (char) (code[i+3]);
             	 if (IDD[i] == 32) {
             	    IDDlen = i;  //i=3
             	   // for (int j=0; j<8; j++) {
             		//    IEEEAddr[j] = code[i+1+j+3];
             	    //}
             	    //for (int j=0; j<5; j++) { //Length if data type like "Light", "Temperature" doesnt over 12
             	 	 //  dataType[j] = (char)code[i+2+8+3+j];
             	    //}
             	  // dataTypeString = String.valueOf(dataType);
             	    for (int j=0; j<3; j++) {
             	    	lightValueBuf[j] = code[i+5+1+j]; //lightValueBuf[j] = code[i+2+8+3+6+j];
             	    	//system.out.println("code9~11 =  " + code[i+5+1+j]);
             	    }
             	   /*for (int j=0; j<4; j++) {
            	    	tempValueBuf[j] = code[i+5+1+3+j]; //lightValueBuf[j] = code[i+2+8+3+6+j];
            	    	System.out.println("code12~15 =  " + code[i+5+1+3+j]);
            	    }*/
             	    break;
             	 }
             	
                 }
                if (bits != 0)
                {
                	// the size of url[] is fixed to 256, so we have to create an array url2 whose length equal to 'urllen'.
             	   // in this way, we can use String.valueOf() to turn the char array into String...
             	  // char [] url2 = new char[urllen];
                    //for (int i=0; i<urllen; i++)
                     	//url2[i] = url[i];
             	   //urlString = String.valueOf(url2);
             	  char [] IDD2 = new char[IDDlen];
                  for (int i=0; i<IDDlen; i++)
                   	IDD2[i] = IDD[i];
           	   	  IDDString = String.valueOf(IDD2);
             	  lightValue = ((((double)lightValueBuf[1]*16*16 + (double)lightValueBuf[2])/4)/4096)*6250*1.5;
             	  tempValue = ((double)tempValueBuf[0]*16*16 + (double)tempValueBuf[1]) * 0.01 -39.6; //溫度經過公式轉換後的結果
             	  double rawHumi = (double)tempValueBuf[2]*16*16 + (double)tempValueBuf[3];  
             	  double humiValue = 0.0405 * rawHumi - 4 - 0.0000028 * (rawHumi * rawHumi);   //濕度經過公式轉換後的結果
             	 //System.out.println("temp: "+ tempValue);
             	 //System.out.println("humi: "+ humiValue);
             	 //sHex = Fun.DecToHex(code, bits);       	
             	   
                  //rString = "OB " + urlString +" "+Fun.DecToHex(IEEEAddr, 8) +" "+dataTypeString+" "+ lightValue ; 
             	  rString = "Data " + IDDString +" "+ lightValue ; //the string that we wanna return, in the future, if we want to return temp or humi value, add here
                  //System.out.println("in Uart:"+rString);
                  //System.out.println("urlString length ="+ urlString.length());
             	  bits = 0;
                }
            }
            else if (code[0] == 'S' && code[1] == 'e') {
               char [] packet = new char[bits];
 			   for (int i=0; i<bits; i++)
             	 packet[i] = (char)code[i];
 			   rString = String.valueOf(packet);
               bits = 0;
            }
                 
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		//return sHex;
		return rString;
	}
	
	public void Close()
	{
		try {
			in.close();
			out.close();
			serialPort.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}