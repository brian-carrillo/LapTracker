package laptracker.utils;


/**
 * Defines an interface for a class that can handle Serial Input
 * The methods are usually called in the order they are declared below, but this may vary by implementation
 */

public interface SerialInputHandler{
	public void didConnectToPort(String portName);
	public void didBeginWaitingForInitMessage();
	public void didReceiveInitMessage();
	public void didReceiveMessage(String s);
	public void didDisconnectFromPort();
	public void setPeripheralVersionNumber(float version);
	public void incorrectPeripheralTypeInserted();
	public void sendTiming(Long[] times, int m);
	public void writeCalibration(String str);
}
