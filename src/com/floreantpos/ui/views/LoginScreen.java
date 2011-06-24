/*
 * LoginScreen.java
 *
 * Created on August 14, 2006, 10:57 PM
 */

package com.floreantpos.ui.views;

import java.util.Calendar;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

import com.coresoftwaresystems.swing.KeypadEvent;
import com.coresoftwaresystems.swing.KeypadEventListener;
import com.coresoftwaresystems.swing.NumericKeypad;
import com.floreantpos.IconFactory;
import com.floreantpos.POSConstants;
import com.floreantpos.main.Application;
import com.floreantpos.model.AttendenceHistory;
import com.floreantpos.model.Shift;
import com.floreantpos.model.User;
import com.floreantpos.model.dao.AttendenceHistoryDAO;
import com.floreantpos.model.dao.UserDAO;
import com.floreantpos.swing.MessageDialog;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.util.PasswordHasher;
import com.floreantpos.util.ShiftUtil;

/**
 *
 * @author  MShahriar
 */
public class LoginScreen extends JPanel {
	public final static String VIEW_NAME = "LOGIN_VIEW";
	
	private final static int LOGIN_USER_PROMPT = 0;
	private final static int LOGIN_PASSWORD_PROMPT = 1;
	
	private final JLabel imageComponent;
	private final NumericKeypad keypad;
	private final JPanel loginPanel;
	private final JLabel promptLabel;
	
	private String userId = null;
	private String userPassword = null;
	private int loginState = 0;
    
	
	/** Creates new form LoginScreen */
	public LoginScreen() {
		setLayout(new MigLayout("ins 20 10 20 10, fill","[fill,growprio 100,grow][]",""));
		
		imageComponent = new JLabel(IconFactory.getIcon("florent-pos.png"));
		imageComponent.setBorder(new EtchedBorder());
		
		add(imageComponent, "spany,grow,flowx");
		
//		passwordScreen = PasswordScreen.getInstance();
//		passwordScreen.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5,5,12,5)));
//		add(passwordScreen, "width 200,growy 100");

		keypad = new NumericKeypad();
		loginPanel = new JPanel();
		promptLabel = new JLabel("USER LOGIN");

		keypad.addKeypadEventListener(new KeypadEventListener() {
	        public void receiveKeypadEvent(KeypadEvent e) {
	        	switch (e.getEventId()) {
	        	case KeypadEvent.ENTER_TRIGGERED:
	        		switch (loginState) {
	        		case LoginScreen.LOGIN_USER_PROMPT:
	        			System.out.println("User ID:" + keypad.getText());
	        			userId = keypad.getText();
	        			keypad.setText("");
	        			keypad.setProtected(true);
	        			promptLabel.setText("ENTER PASSWORD");
	        			loginState = LoginScreen.LOGIN_PASSWORD_PROMPT;
	        			break;
	        		case LoginScreen.LOGIN_PASSWORD_PROMPT:
	        			System.out.println("Password:" + keypad.getText());
	        			userPassword = keypad.getText();
	        			keypad.setText("");
	        			keypad.setProtected(false);
	        			
	        			try {
		        			doLogin();	        				
	        			} catch (Exception loginException) {
	        				MessageDialog.showError(loginException.getMessage());
	        				System.out.println("Not authenticated...");
	        			}	        			
	        			promptLabel.setText("USER LOGIN");
	        			loginState = LoginScreen.LOGIN_USER_PROMPT;	        				
	        			keypad.setText("");
	        			userId = "";
	        			userPassword = "";
	        		}
	        		break;
	        	}
//	        	setVisible(true);
	        }
	    });

		
		loginPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5,5,12,5)));
		loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.PAGE_AXIS));
		
		add(loginPanel, "width 300,growy 100,flowx");
		
		promptLabel.setFont(new java.awt.Font("Tahoma", 1, 24));
		promptLabel.setForeground(new java.awt.Color(204, 102, 0));
		promptLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		promptLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		loginPanel.add(promptLabel);
		loginPanel.add(keypad);	
		
	}
	
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
	}
	
	private void doLogin() throws RuntimeException {
		
		Application application = Application.getInstance();

		UserDAO dao = new UserDAO();
		User user = dao.findUser(Integer.valueOf(userId));
		if (!user.getPassword().equals(PasswordHasher.hashPassword(userPassword))) {
			throw new RuntimeException(POSConstants.WRONG_PASSWORD);
		}

		Shift currentShift = ShiftUtil.getCurrentShift();
		if (currentShift == null) {
			throw new RuntimeException(POSConstants.NO_SHIFT_CONFIGURED);
		}

		adjustUserShift(user, currentShift);

		application.setCurrentUser(user);
		application.setCurrentShift(currentShift);

		application.getRootView().showView(SwitchboardView.VIEW_NAME);
		
	}

	private void adjustUserShift(User user, Shift currentShift) {
		Application application = Application.getInstance();
		Calendar currentTime = Calendar.getInstance();

		if (user.isClockedIn() != null && user.isClockedIn().booleanValue()) {
			Shift userShift = user.getCurrentShift();
			Date userLastClockInTime = user.getLastClockInTime();
			long elaspedTimeSinceLastLogin = Math.abs(currentTime
					.getTimeInMillis() - userLastClockInTime.getTime());

			if (userShift != null) {
				if (!userShift.equals(currentShift)) {
					reClockInUser(currentTime, user, currentShift);
				} else if (userShift.getShiftLength() != null
						&& (elaspedTimeSinceLastLogin >= userShift
								.getShiftLength())) {
					reClockInUser(currentTime, user, currentShift);
				}
			} else {
				user.doClockIn(application.getTerminal(), currentShift,
						currentTime);
			}
		} else {
			user.doClockIn(application.getTerminal(), currentShift, currentTime);
		}
	}

	private void reClockInUser(Calendar currentTime, User user,
			Shift currentShift) {
		POSMessageDialog
				.showMessage("You will be clocked out from previous Shift");

		Application application = Application.getInstance();
		AttendenceHistoryDAO attendenceHistoryDAO = new AttendenceHistoryDAO();

		AttendenceHistory attendenceHistory = attendenceHistoryDAO
				.findHistoryByClockedInTime(user);
		if (attendenceHistory == null) {
			attendenceHistory = new AttendenceHistory();
			Date lastClockInTime = user.getLastClockInTime();
			Calendar c = Calendar.getInstance();
			c.setTime(lastClockInTime);
			attendenceHistory.setClockInTime(lastClockInTime);
			attendenceHistory.setClockInHour(Short.valueOf((short) c
					.get(Calendar.HOUR)));
			attendenceHistory.setUser(user);
			attendenceHistory.setTerminal(application.getTerminal());
			attendenceHistory.setShift(user.getCurrentShift());
		}

		user.doClockOut(attendenceHistory, currentShift, currentTime);

		user.doClockIn(application.getTerminal(), currentShift, currentTime);
	}

}
