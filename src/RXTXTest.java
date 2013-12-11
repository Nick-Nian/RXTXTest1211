import java.util.Scanner;

public class RXTXTest {
	private Uart uart = null;
	private final String UART_PORT = "/dev/ttyUSB3";
	
	public static void main(String[] args)
	{
		Uart.PortList(); // port list
		(new RXTXTest()).Run();
	}
	
	public void Run()
	{
		InitUart();
		ListenZPacket();
		// Transmit data
		Scanner scanner = new Scanner(System.in);
		String cmd = "";
		//byte cmd[];
		
		while (true)
		{
			cmd = scanner.nextLine();
			
			//DF command => cmd = "44";
			//OB command => cmd = "4F";
			uart.Send(cmd);
			Sleep(1000);
		}
	}
	
	public void InitUart()
	{
		uart = new Uart();
		uart.Open(UART_PORT);
	}
	
	public void ListenZPacket() // ZPacket form ZigBee
	{		
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{   
					
					// Receive data					
					String rawPkt = uart.Recv();
					if (rawPkt.length() != 0)
					{
						//System.out.println("Packet type:");
						//System.out.println(rawPkt);
						//���Φr��
						String[] msg = rawPkt.split(" ");
						if (msg[0].equals("Register")){
							   System.out.println("Packet type:" + msg[0]);
							   System.out.println("Node type:" + msg[1]);
							   System.out.println("Node ID:"+msg[2]);
							   System.out.println("Sensing type:"+msg[3]);							   
							}
							else if (msg[0].equals("Data")) {
								System.out.println();
								System.out.println("*************************");
								System.out.println("*   "+"Packet type:" + msg[0]);
								System.out.println("*   " +"Node ID:"+msg[1]);
								//System.out.println("IEEE Address:"+msg[2]);
								//System.out.println("Data type:"+msg[3]);  
							    System.out.println("*   " +"Raw data:"+msg[2]);
							    System.out.println("*************************");
							}
							else if (msg[0].equals("Sensing")){
								System.out.println("*********************************");
								System.out.println("*                               *");
								System.out.println("*   " + rawPkt + "   *");
								System.out.println("*                               *");
								System.out.println("*********************************");
							}
							else if (msg[0].equals("PIR")){
								if (msg[2].equals("YY")){
									System.out.println("YY");
								}else if (msg[2].equals("NN")){
									System.out.println("NN");
								}
							}
							else if (msg[0].equals("CO2")){
									System.out.println("CO2's Concentration: " + msg[1]);
							}
					}
					Sleep(100);
				}
			}
		});
		t.start();
	}
	
	public void Sleep(int ms)
	{
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {}
	}
}
