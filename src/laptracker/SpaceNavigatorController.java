package laptracker;

import net.java.games.input.Component; 
import net.java.games.input.Controller; 
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin; 

/** 
 * Details for: SpaceNavigator, Stick, Unknown 
 * Components: (8) 
 * 0. Z Axis, z, relative, analog, 0.0 
 * 1. Y Axis, y, relative, analog, 0.0 
 * 2. X Axis, x, relative, analog, 0.0 
 * 3. Z Rotation, rz, relative, analog, 0.0 
 * 4. Y Rotation, ry, relative, analog, 0.0 
 * 5. X Rotation, rx, relative, analog, 0.0 
 * 6. Button 0, 0, absolute, digital, 0.0 
 * 7. Button 1, 1, absolute, digital, 0.0 
 * No Rumblers 
 * No subcontrollers 
 */ 

public class SpaceNavigatorController { 

	public static final int NUM_BUTTONS = 2; 

	public static final int ZAXIS = 0; 
	public static final int YAXIS = 1; 
	public static final int XAXIS = 2; 
	public static final int ZROTATION = 3; 
	public static final int YROTATION = 4; // default value 
	public static final int XROTATION = 5; 
	public static final int BUTTON0 = 6; 
	public static final int BUTTON1 = 7; 

	private int xAxisIdx, yAxisIdx, zAxisIdx, rxAxisIdx, ryAxisIdx, rzAxisIdx; 
	private int buttonsIdx[]; 

	private Controller controller; 
	private Component[] comps;
	private boolean mouseConnected;
	private boolean pollingValue;

	public SpaceNavigatorController () { 
		
		 // Moved code in the constructor to the method checkControllers so it can be run ore than once
		this.checkControllers();
//		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment(); 
//		Controller[] cs = ce.getControllers(); 
//		if (cs.length == 0) { 
//			System.out.println("No controllers found"); 
//			return; 
//		} else System.out.println("Num. controllers: " + cs.length); 
//
//		controller = findSpaceNavigator(cs); 
//		if (controller != null){
//			mouseConnected = true;
//			System.out.println("Space Navigator controller: " + controller.getName() + ", " + controller.getType()); 
//
//			findCompIndices(controller);
//		}
	} 


	private Controller findSpaceNavigator (Controller[] cs) { 
		int i = 0; 
		while (i < cs.length) { 
			cs[i].getType(); 
			if (cs[i].getName().equals("SpaceNavigator for Notebooks"))
				break; 
			i++; 
		} 

		if (i == cs.length) { 
			System.out.println("No Space Navigator found"); 
			mouseConnected = false;
			return null; 
		} else System.out.println("Space Navigator index: " + i); 

		return cs[i]; 
	} 

	private void findCompIndices (Controller controller) 
	/* Store the indices for the analog sticks axes 
(x,y) and (z,rz), POV hat, and 
button components of the controller. 
	 */ { 
		comps = controller.getComponents(); 
		if (comps.length == 0) { 
			System.out.println("No Components found"); 
			System.exit(0); 
		} else System.out.println("Num. Components: " + comps.length); 

		// get the indices for the axes of the analog sticks: (x,y) and (z,rz) 
		xAxisIdx = findCompIndex(comps, Component.Identifier.Axis.X, "x"); 
		yAxisIdx = findCompIndex(comps, Component.Identifier.Axis.Y, "y"); 
		zAxisIdx = findCompIndex(comps, Component.Identifier.Axis.Z, "y"); 

		rxAxisIdx = findCompIndex(comps, Component.Identifier.Axis.RX, "rx"); 
		ryAxisIdx = findCompIndex(comps, Component.Identifier.Axis.RY, "ry"); 
		rzAxisIdx = findCompIndex(comps, Component.Identifier.Axis.RZ, "rz"); 

		findButtons(comps); 
	} 

	private int findCompIndex (Component[] comps, Component.Identifier id, String nm) { 
		Component c; 
		for (int i = 0; i < comps.length; i++) { 
			c = comps[i]; 
			if ((c.getIdentifier() == id)) { 
				System.out.println("Found " + c.getName() + "; index: " + i); 
				return i; 
			} 
		} 

		System.out.println("No " + nm + " component found"); 
		return -1; 
	} 

	/** 
	 * Search through comps[] for NUM_BUTTONS buttons, storing 
	 * their indices in buttonsIdx[]. Ignore excessive buttons. 
	 * If there aren't enough buttons, then fill the empty spots in 
	 * buttonsIdx[] with -1's. 
	 */ 
	private void findButtons (Component[] comps) { 
		buttonsIdx = new int[NUM_BUTTONS]; 
		int numButtons = 0; 
		Component c; 

		for (int i = 0; i < comps.length; i++) { 
			c = comps[i]; 
			if (isButton(c)) { // deal with a button 
				if (numButtons == NUM_BUTTONS) // already enough buttons 
					System.out.println("Found an extra button; index: " + i + ". Ignoring it"); 
				else { 
					buttonsIdx[numButtons] = i; // store button index 
					System.out.println("Found " + c.getName() + "; index: " + i); 
					numButtons++; 
				} 
			} 
		} 

		// fill empty spots in buttonsIdx[] with -1's 
		if (numButtons < NUM_BUTTONS) { 
			System.out.println("Too few buttons (" + numButtons + "); expecting " + NUM_BUTTONS); 
			while (numButtons < NUM_BUTTONS) { 
				buttonsIdx[numButtons] = -1; 
				numButtons++; 
			} 
		} 
	} // end of findButtons() 

	/** 
	 * Return true if the component is a digital/absolute button, and 
	 * its identifier name ends with "Button" (i.e. the 
	 * identifier class is Component.Identifier.Button). 
	 */ 
	private boolean isButton (Component c) { 
		if (!c.isAnalog() && !c.isRelative()) { // digital and absolute 
			String className = c.getIdentifier().getClass().getName(); 
			// System.out.println(c.getName() + " identifier: " + className); 
			if (className.endsWith("Button")) return true; 
		} 
		return false; 
	} 

	/** 
	 * Return all the buttons in a single array. Each button value is 
	 * a boolean. 
	 */ 
	public boolean[] getButtons () { 
		boolean[] buttons = new boolean[NUM_BUTTONS]; 
		float value; 
		for (int i = 0; i < NUM_BUTTONS; i++) { 
			value = comps[buttonsIdx[i]].getPollData(); 
			buttons[i] = ((value == 0.0f) ? false : true); 
		} 
		return buttons; 
	} // end of getButtons() 


	public boolean isButtonPressed (int pos) 
	/* Return the button value (a boolean) for button number 'pos'. 
pos is in the range 1-NUM_BUTTONS to match the game pad 
button labels. 
	 */ { 
		if ((pos < 1) ||(pos > NUM_BUTTONS)) { 
			System.out.println("Button position out of range (1-" + NUM_BUTTONS + "): " + pos); 
			return false; 
		} 

		if (buttonsIdx[pos - 1] == -1) // no button found at that pos 
			return false; 

		float value = comps[buttonsIdx[pos - 1]].getPollData(); 
		// array range is 0-NUM_BUTTONS-1 
		return ((value == 0.0f) ? false : true); 
	} // end of isButtonPressed() 

	public void poll () { 
		if (controller != null)
			if(!controller.poll()){
				mouseConnected = false;
			}
	} 


	/** 
	 * X Translation 
	 * 
	 * @return float value between 1613 and -1613 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getTX () { 
		return comps[xAxisIdx].getPollData(); 
	} 

	/** 
	 * Y Translation 
	 * 
	 * @return float value between 1613 and -1613 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getTY () { 
		return comps[yAxisIdx].getPollData(); 
	} 

	/** 
	 * Z Translation 
	 * 
	 * @return float value between 1613 and -1613 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getTZ () { 
		return comps[zAxisIdx].getPollData(); 
	} 


	/** 
	 * X Rotation 
	 * 
	 * @return float value between 1560 and -1560 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getRX () { 
		return comps[rxAxisIdx].getPollData(); 
	} 

	/** 
	 * Y Rotation 
	 * 
	 * @return float value between 1560 and -1560 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getRY () { 
		return comps[ryAxisIdx].getPollData(); 
	} 

	/** 
	 * Z Rotation 
	 * 
	 * @return float value between 1560 and -1560 
	 * <p> 
	 * Note: the returned value my not be very logic and correct, I just measured them during pooling 
	 */ 
	public float getRZ () { 
		return comps[rzAxisIdx].getPollData(); 
	} 
	public boolean isConnected () {          // Return boolean value which detects if mouse is connected
		return(mouseConnected); 
	} 
	public void checkControllers(){
		// This method checks for connected controllers and looks for the space mouse
		// If the space mouse is connected it will return the indices
		Controller[] cs;
		 
		DirectAndRawInputEnvironmentPlugin directEnv = new DirectAndRawInputEnvironmentPlugin();
		if (directEnv.isSupported()) {
		    cs = directEnv.getControllers();
		} else {
		    cs = ControllerEnvironment.getDefaultEnvironment().getControllers();
		}
		
		if (cs.length == 0) { 
			System.out.println("No controllers found"); 
			return; 
		} else System.out.println("Num. controllers: " + cs.length);
		controller = findSpaceNavigator(cs);
		if (controller != null){
			mouseConnected = true;
			System.out.println("mouse true1");
			System.out.println("Space Navigator controller: " + controller.getName() + ", " + controller.getType()); 

			findCompIndices(controller);
		}
		
	}
} 