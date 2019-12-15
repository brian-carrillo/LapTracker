package laptracker.utils;
import java.lang.ref.WeakReference;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;



/**
 * This is a modified example class for providing two way communication through a serial port
 * It contains convenience methods for discovering and connecting to serial ports, as well as two threads that handle asynchronous reading and writing
 */
public class TwoWaySerialComm	
{
	private WeakReference<SerialInputHandler> handler;		// a serial input handler, if any, that can deal with received messages
	public boolean connectedPort = false;		// whether a port is connected
	public boolean portLost = false;			// whether a connected port connection was lost
	private boolean calFile;					// Flag to write calibration file before collecting data
	private boolean handShakeBegun = false;				// Whether the hand shaking protocol has begun
	private boolean isRunning = false;			// flag if correct device is connected
	private boolean resetArduino = false;
	private boolean firstWrite = true;

	private SerialPort commPort;				// the connected port
	
	private static final Object stopReadMutex =  new Object();		// object used to synchronize starting and stopping threads
	private static final Object stopWriteMutex =  new Object();		// object used to synchronize starting and stopping threads
	private boolean stopRead = false;		// stops reading
	private boolean stopWrite = false;		// stops writing

	protected TwoWaySerialComm serial;		// an instance of this class
	protected Thread serialOut;				// thread to write to the serial
	protected Thread serialIn;				// thread to read from the serial
	
//	private boolean oldVersion = false;

	private final static int timeoutTime = 3000;
	public final static int timeout = 50;			// timeout before assumed to not be otosim sensor (in asterix's / 100 ms intervals (I think))
	final boolean debug = true;

 /**
  * Basic constructor, with no handler
  */
  	public TwoWaySerialComm ()
	{
		this (null);						//simply calls the default constructor, with no handler
	}
 /**
  * Default constructor, takes one handler as optional input (handler may be null as above)
  */
	public TwoWaySerialComm (WeakReference<SerialInputHandler> handler)
	{
		this.handler = handler;				// registers the SerialInputHandler
		this.serial = this;
	}
 /**
  * Disconnects from the connected serial port
  */
	public void disconnect () throws Exception
	{
		synchronized(stopReadMutex)		// stops reading from the port
			{stopRead = true;
			resetArduino = true;
			}
		Thread.sleep(200);				// Sleep to allow time for serial writer to send 's' in order to reset the arduino
		
		synchronized(stopWriteMutex)	// stops writing to the port
			{stopWrite = true;
			}
		
		if (commPort != null && commPort.isOpened())
			commPort.closePort();				// closes the ports
		
		if (connectedPort && handler != null)
			handler.get().didDisconnectFromPort();
		connectedPort = false;			// changes status to unconnected
		portLost = false;
	}

 /**
  * Connects to the given serial port
  * This method also creates two new threads to deal with reading (SerialReader) and writing (SerialWriter) to the port
  * @param portName		the name of the port to connect to
  */
    public void connect ( String portName, int sleepTime ) throws Exception
    {
    	commPort = new SerialPort(portName);// gets the port
        if ( commPort.isOpened() )		// checks if the port is being used
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            commPort.openPort();			// attempts to open the port, with a 2000ms timeout
            commPort.setParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);	//the first parameter is the baud rate of the connection (I have no idea what the other are)

            commPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
            commPort.setDTR(false);
			commPort.setRTS(true);

            serialIn = new Thread(new SerialReader(commPort));			// creates and starts SerialReader and SerialWriter threads
            serialOut = new Thread(new SerialWriter(commPort, sleepTime));
            calFile = true;
            firstWrite = true;
            serialOut.start();							// start writing to arduino to connect
        	serialIn.start();							// starts reading (to check for version info, etc)
        }
    }

 /**
  * This class reads input from the given InputStream, and then either prints out the message (for debugging), or hands it to an attach SerialInputHandler
  * This code will actually work for any InputStream, so may be useful later
  */
    public class SerialReader implements Runnable
    {
        SerialPort in;			// the InputStream to read from

		//boolean isRunning = false;		// flag if this is an Optosim
		Long[] timeStamp = new Long[200000];

		/**
		 * Basic constructor, InputStream required
		 */
        public SerialReader ( SerialPort in)
        {
			synchronized(stopReadMutex)			// starts reading
				{stopRead = false;}
            this.in = in;
        }

        public void run ()
        {
        	int len = 1;						// used to check if there is anything new in the buffer, and the length of input strings
            String newBuf = new String();		// New string buffer, reads strings from the serial port and passes them to the String Builder
           // String dataLine = new String();
           // String remainder = new String();
           // int newLineIndex;
            boolean running = true;
            StringBuilder data = new StringBuilder(); // Stores data collected from the serial port as strings

            try
            {
				while ( running )
				{
					if(stopRead){
						break;
					}
					if (isRunning)				// if this is an OtoSim sensor
					{
						dataReadLoop:
						while ( running ) {		// while there is new data
							
							synchronized(stopReadMutex){
								if(stopRead){     // break loop with disconnect is called
									isRunning = false;
									break dataReadLoop;
								}
								try{
									newBuf = this.in.readString(len, timeoutTime);  // read in string from serial line with length len
								}catch(SerialPortTimeoutException e){
									System.out.println("Read String Timeout Error: Serial Port Disconnected");
									try {
										serial.disconnect();
									} catch (Exception e1) {
										e1.printStackTrace();
									}
								}
							}
							
							if(newBuf.equals("r")){
								newBuf = "";
								data = new StringBuilder();         // reset the string builder
							}
							if(newBuf.equals("q")){					// if we receive a q from the arduino, break the loop
								System.out.println("Received char:" + newBuf);
								firstWrite = true;
								calFile = true;
								newBuf = "";
								data = new StringBuilder();         // reset the string builder
							}

							if (handler == null && debug)		// if there is no handler...
								System.out.print(newBuf);        // ... print out input immediately (System.out.print deals with new lines on it's own)
							else
							{
								if (newBuf != null){
									data.append(newBuf);		// append each character to the string builder
								}
				//---------------------------------------------------------------------------------------------------------------------------------------------				
					// Attempt to read multiple characters at a time from the arduino,
				   // There is a timing issue with this method
//								newLineIndex = data.indexOf("\n");
//									
//								if (newLineIndex != -1){
//									dataLine = data.substring(0,newLineIndex);
//									
//									if(calFile){							// Write calibration file first, then collect data
//										calFile = false;
//										handler.get().writeCalibration(dataLine);
//									}
//									else{
//										handler.get().didReceiveMessage(dataLine);
//									}
//									
//									if(data.length()-1 == newLineIndex){
//										data = new StringBuilder();
//									}else{
//										remainder = data.substring(newLineIndex+1);
//										data = new StringBuilder(remainder);
//									}
//									
//								}
			//---------------------------------------------------------------------------------------------------------------------------------------------				
								
						// Old reading code, reads one character at a time			
								if (data.length() > 1 && data.charAt(data.length()-1) == '\n'){ // when the input is \n then save the line of data to a csv file
									if(calFile){							// Write calibration file first, then collect data
										calFile = false;
										handler.get().writeCalibration(data.toString());
									}
									else{
										handler.get().didReceiveMessage(data.toString());
									}
									data = new StringBuilder();                       // after saving the line, reset the stringbuilder
								}
									
							}
						}
					}
					else if (handShakeBegun)
					{
						if (debug)
							System.out.print("Checking for proper version information: ");
						for ( int i = 0; i < timeout; i++ )								 // limited number of checks
						{
							if (running)
								newBuf = null;
								try {
									newBuf = this.in.readString(len, timeoutTime);   // read serial line one char at a time
								} catch (SerialPortTimeoutException ignored) {
									System.out.print("ReadString timeout2 : Serial port disconnected");
									try {
										serial.disconnect();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							if (newBuf != null)
								data.append(newBuf);								// append the current char in the buffer to the stringbuilder
		
							if (debug){
								System.out.println("*"+ data.toString());        // print contents of stringbuilder
							}
							if (data.toString().equals("r") )					// check if the arduino sent the char 'r'
							{
								/******** VERSION INFORMATION HERE **********/
					
								isRunning = true;					// starts regular input checking
								connectedPort = true;
								if (debug)
									System.out.println("check completed");
								handler.get().didConnectToPort(null);
								data = new StringBuilder();
								//serialOut.start();
								break;
							}
							try {									// sleeps a bit to give the arduino time
								Thread.sleep(100);
							} catch (Exception e){}
						}
						if (!isRunning)								// if we couldn't find any signs that this is an OtoSim sensor
						{
							if (debug)
								System.out.println(" Input timed out!");
							running = false;
							try
							{
								serial.disconnect();				// attempts to disconnect
							} catch (Exception ex){}
							Thread.currentThread().interrupt();		// interrupts this thread
						}
					}
					Thread.sleep(200); // Sleep thread when not reading data from arduino
					//System.out.println("Waiting to send data....");
					
				}
			}
			catch ( SerialPortException e )					// for debugging only, hopefully
			{
                System.out.println(e);
				//running = false;
				try
				{
					serial.disconnect();			// attempts to disconnect
					System.out.println("dis1");
				} catch (Exception ex){}
                Thread.currentThread().interrupt();		// interrupts the current thread
			}
//            catch ( SerialPortTimeoutException e ) {
//                running = false;
//				try
//				{
//					serial.disconnect();			// attempts to disconnect
//					System.out.println("dis2");
//				} catch (Exception ex){}
//                Thread.currentThread().interrupt();		// interrupts the current thread
//            }
            catch (InterruptedException e) {
				// TODO Auto-generated catch block for thread sleep
				e.printStackTrace();
			}
        }
    public Long[] getTiming(){
    	return timeStamp;
    }
    }

 /**
  * This class writes to a given OutputStream (which is supposed to correspond to an arduino's serial port
  * So far, it writes the letter 's' at a regular interval to request updates from the arduino
  * However, it should be relatively easy to add new functionality, such as a software reset command
  */
    public class SerialWriter implements Runnable
    {
        SerialPort out;		// the OutputStream to write to
        private int sleepLength = 20;
        private boolean writting;

		 /**
		  * Basic constructor, OutputStream required
		  */
        public SerialWriter ( SerialPort out, int sleepTime )
        {
			synchronized(stopWriteMutex)		// starts writing
				{
				stopWrite = false;
				}
            this.out = out;
            this.sleepLength = sleepTime;
            this.writting = true;
        }

        public void run ()
        {
            try
            {
				while (writting)	// runs forever [can change to other boolean if stopping is required, or just kill it and spawn a new thread =) ]
				{
					if(firstWrite){
						this.out.writeBytes(("d").getBytes());  	// writes out 'd', get DATA
						System.out.println("Sent Char: d");
						synchronized(stopWriteMutex){
							firstWrite = false;
							handShakeBegun = true;	
						}
					}
					
					synchronized(stopWriteMutex){
						if(stopWrite){
							break;
						}
						if(resetArduino){
							this.out.writeBytes(("s").getBytes());     // write "s" to tell arduino to stop sending data
							System.out.println("Sent Char: s");
							Thread.sleep(200);	// delay after writing before exiting thread
							resetArduino = false;
						}
					}
					
					//Thread.sleep(sleepLength);					// sleeps to avoid spamming the arduino (and previously, to avoid undermining the input filtering)
					try //- delay for CPU WASTAGE
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
            }
            catch ( Exception e )		// for debug purposes only, hopefully
            {
            	System.out.println(e);
            	System.out.println("Error in serial Write thread");
                try
                {
					//serial.disconnect();	// attempts to disconnect
					System.out.println("dis3");
				}
				catch (Exception ex){}
                Thread.currentThread().interrupt();		// interrupts the current thread
            }
        }
    }
 /**
  * Returns a vector containing the String names of every available SERIAL port
  * Each element returned can be directly used as an argument to the connect (String s) method
  */
	public String[] getPortList()
	{
		String[] portList = SerialPortList.getPortNames();				// stores a list of the available SERIAL ports and gets the name of ALL ports (serial or parallel) available

		if (debug) {
			System.out.println("Found the following ports:");				// prints out the discovered ports
			for (int i = 0; i < portList.length; i++)
				System.out.println(("   " + portList[i]));
			if(portList.length==0){
				System.out.println("No ports Found, please connect box to USB port");
			}
		}

		return portList;												// returns the ports
	}
	
	public void startDevice(){
		synchronized(stopWriteMutex)	// stops writing to the port
		{
			resetArduino = true;
		}
	}
	


 /**
  * This method is for debugging only, and should not be called unless direct monitoring of the serial connection is needed
  * Connects to the first available sensor and prints any output, while handling disconnects and reconnects (good sample code)
  */
   /* public static void main ( String[] args )
    {
		try
        {
		  	TwoWaySerialComm serial = new TwoWaySerialComm();	// creates a new instance of this class with no handler
		  	Vector<String> ports;

		  	while (!serial.connectedPort)						// while it's not connected
		  	{
				int i = 0;
				ports = serial.getPortList();			// gets the ports available
				for (i = 0; i < ports.size(); i++)
				{
					String s = ports.get(i);
					try
					{
						while (serial.connectedPort)	// kills time to avoid overwriting a connected port
							Thread.sleep(100);

						if (serial.portLost)
							serial.disconnect();		// if it didn't work, disconnects
						System.out.println("Attempting to connect to port "+s);
						serial.connect(s);				// connects to each port
						Thread.sleep(200*timeout);
						if (serial.portLost)
							serial.disconnect();		// if it didn't work, disconnects
					}
					catch (Exception e){}
				}
				try {
					Thread.sleep(200*timeout);
				}
				catch (Exception e){}
			}
        }
        catch ( Exception e ){}
    }*/
	

}
