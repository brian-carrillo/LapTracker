package laptracker;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;


public class SpaceMouse extends JFrame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4344387915827136563L;

	private static  SpaceNavigatorController spaceNavigatorController;

	public SpaceMouse(){
		spaceNavigatorController = new SpaceNavigatorController(); 

	}

	public void poll() { 
		spaceNavigatorController.poll();
	}

	public float getTX(){
		return(spaceNavigatorController.getTX());
	}	

	public float getTY(){
		return(spaceNavigatorController.getTY());
	}	

	public float getTZ(){
		return(spaceNavigatorController.getTZ());
	}	

	public float getRX(){
		return(spaceNavigatorController.getRX());
	}	

	public float getRY(){
		return(spaceNavigatorController.getRY());
	}	

	public float getRZ(){
		return(spaceNavigatorController.getRZ());
	}	
	public String getFstr(){
		return(Float.toString(this.getTX()) + ',' + Float.toString(this.getTY()) + ',' + Float.toString(this.getTZ()) + ',' + Float.toString(this.getRX()) + ',' + Float.toString(this.getRY()) + ',' + Float.toString(this.getRZ()));
	}
	public String getFstr2(long time){
		return(Float.toString(this.getTX()/time) + ',' + Float.toString(this.getTY()/time) + ',' + Float.toString(this.getTZ()/time) + ',' + Float.toString(this.getRX()/time) + ',' + Float.toString(this.getRY()/time) + ',' + Float.toString(this.getRZ()/time));
	}
	public float[] getWeight(){
		float[] data = {this.getTX(),this.getTY(), this.getTZ(),this.getRX(), this.getRY(), this.getRZ()};
		return(data);
	}
	public String convertArray(float[] data){
		return(Float.toString(data[0]) + ',' + Float.toString(data[1]) + ',' + Float.toString(data[2]) + ',' + Float.toString(data[3]) + ',' + Float.toString(data[4]) + ',' + Float.toString(data[5]));
	}
	public boolean isConnected(){
		return(spaceNavigatorController.isConnected());
	}
	public void checkController(){
		spaceNavigatorController.checkControllers();
	}
	public static void main(String[] args) {
	
		new SpaceMouse();
		
	}

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	} 
}
