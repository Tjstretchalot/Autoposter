package me.timothy.autoposter.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.json.simple.parser.ParseException;

import com.github.jreddit.user.User;
import com.github.jreddit.utils.restclient.HttpRestClient;
import com.github.jreddit.utils.restclient.RestClient;

public class AutoposterPanel extends JPanel {
	private static final long serialVersionUID = -4692762780707236888L;

	private static final long MILLISECONDS_PER_SECOND = 1000;
	private static final long MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
	private static final long MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
	private static final long MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;

	private Timer timer;
	private List<Runnable> runnables;
	
	private JPanel top, bottom, centertop, center, left, right;
	
	private JLabel nextPost;
	private JButton submitNow;
	
	private JTextField username;
	private JPasswordField password;
	private JCheckBox editLoginInfo;
	private JButton updateLoginInfo;
	
	private JLabel postTitleLabel;
	private JTextField postTitle;
	private JLabel subredditLabel;
	private JTextField subreddit;
	private JTextArea text;
	private JComboBox<String> day;
	private JComboBox<String> hour;
	
	private JCheckBox pause;
	
	private char[] rememberedUsername;
	private char[] rememberedPassword;
	
	public AutoposterPanel() {
		initTimer();
		initComponents();
		prepareComponents();
	}
	
	protected void initTimer() {
		runnables = Collections.synchronizedList(new ArrayList<Runnable>());
		timer = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(pause.isSelected())
					return;
				synchronized(runnables) {
					for(Runnable r : runnables) {
						r.run();
					}
				}
			}
			
		});
		timer.start();
	}
	
	protected void initComponents() {
		nextPost = new JLabel();
		runnables.add(new Runnable() {
			@Override
			public void run() {
				Calendar next = calculateDateAndTimeNext();
				Calendar current = Calendar.getInstance();
				
				if(next.before(current)) {
					nextPost.setText("Should have already happened!");
				}else if(next.after(current)) {
					nextPost.setText("Next post in" + format(next.getTimeInMillis() - current.getTimeInMillis()));
				}else {
					nextPost.setText("Now!");
					doSubmit();
				}
			}
		});
		submitNow = new JButton("Submit now!");
		submitNow.setEnabled(false);
		submitNow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doSubmit();
			}
			
		});
		username = new JTextField(15);
		username.setText("Username");
		password = new JPasswordField(15);
		password.setText("Password");
		
		editLoginInfo = new JCheckBox("Edit login info");
		editLoginInfo.setSelected(true);
		editLoginInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bottom.setVisible(editLoginInfo.isSelected());
				if(!editLoginInfo.isSelected()) {
					username.setText("Username");
					password.setText("Password");
				}else {
					rememberedUsername = null;
					rememberedPassword = null;
					pause.setSelected(true);
					submitNow.setEnabled(false);
					
					nextPost.setText("");
				}
			}
		});
		updateLoginInfo = new JButton("Update login info");
		updateLoginInfo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				bottom.setVisible(false);
				rememberedUsername = username.getText().toCharArray();
				rememberedPassword = password.getPassword();
				User u = verifyUser();
				if(u == null) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, "Failed to login! Try again..", "Invalid Credentials", JOptionPane.ERROR_MESSAGE);
						}
						
					});
					return;
				}
				
				editLoginInfo.setSelected(false);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "Login credentials saved. Check edit login info to clear");
					}
					
				});
				
			}
			
		});
		postTitleLabel = new JLabel("Post Title: ");
		postTitle = new JTextField(15);
		postTitle.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}
			
		});
		subredditLabel = new JLabel("Subreddit: ");
		subreddit = new JTextField(15);
		subreddit.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}
			
		});
		
		text = new JTextArea(50, 50);
		text.setText("Self-text here");
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				nextPost.setText("");
				pause.setSelected(true);
				submitNow.setEnabled(false);
			}
			
		});
		day = new JComboBox<>(new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Every day"});
		hour = new JComboBox<>(new String[]{"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", 
		"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", 
		"20:00", "21:00", "22:00", "23:00"});
		pause = new JCheckBox("Pause");
		pause.setSelected(true);
		pause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(rememberedUsername == null || rememberedPassword == null) {
					pause.setSelected(true);
					submitNow.setEnabled(false);
					JOptionPane.showMessageDialog(null, "You must first input your login credentials", "Cannot unpause", JOptionPane.WARNING_MESSAGE);
				}else if(postTitle.getText().length() == 0) {
					pause.setSelected(true);
					submitNow.setEnabled(false);
					JOptionPane.showMessageDialog(null, "You must first state the post title");
				}else if(subreddit.getText().length() == 0) {
					pause.setSelected(true);
					submitNow.setEnabled(false);
					JOptionPane.showMessageDialog(null, "You must first state the subreddit");
				}
				submitNow.setEnabled(true);
			}
			
		});
		rememberedUsername = null;
		rememberedPassword = null;
		
		top = new JPanel();
		bottom = new JPanel();
		left = new JPanel();
		right = new JPanel();
		center = new JPanel();
		centertop = new JPanel();
	}

	protected User verifyUser() {
		RestClient rClient = new HttpRestClient();
		rClient.setUserAgent("Tjstretchalot's Autoposter for Moderators");
		
		User user = new User(rClient, new String(rememberedUsername), new String(rememberedPassword));
		try {
			user.connect();
			return user;
		}catch(Exception e) {
			return null;
		}
	}
	protected void doSubmit() {
		RestClient rClient = new HttpRestClient();
		rClient.setUserAgent("Tjstretchalot's Autoposter for Moderators");
		
		User user = new User(rClient, new String(rememberedUsername), new String(rememberedPassword));
		try {
			user.connect();
		}catch(Exception e) {
			user = null;
		}
		if(user == null) {
			JOptionPane.showMessageDialog(null, "Failed to submit post! Null user!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			user.submitSelfPost(postTitle.getText(), text.getText(), subreddit.getText());
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to submit post: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void prepareComponents() {
		setLayout(new BorderLayout());
		top.add(nextPost);
		top.add(pause);
		top.add(editLoginInfo);
		top.add(submitNow);
		centertop.setLayout(new GridLayout(2, 2));
		
		JPanel postTitleP = new JPanel();
		postTitleP.add(postTitleLabel);
		postTitleP.add(postTitle);
		centertop.add(postTitleP);
		
		JPanel subredditP = new JPanel();
		subredditP.add(subredditLabel);
		subredditP.add(subreddit);
		centertop.add(subredditP);
		
		JPanel tmp = new JPanel();
		tmp.add(day);
		centertop.add(tmp);
		tmp = new JPanel();
		tmp.add(hour);
		centertop.add(tmp);
		
//		centertop.add(editLoginInfo);
		
		center.add(centertop);
		JScrollPane sPane = new JScrollPane(text);
		center.add(sPane);
		
		bottom.add(username);
		bottom.add(password);
		bottom.add(updateLoginInfo);
		
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(left, BorderLayout.WEST);
		add(right, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
	}
	
	protected Calendar calculateDateAndTimeNext() {
		Calendar c = new GregorianCalendar();
		
		c.setTimeZone(TimeZone.getDefault());
		c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		c.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		c.set(Calendar.HOUR_OF_DAY, hour.getSelectedIndex());
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		int daySelected = day.getSelectedIndex();
		if(daySelected < 7) {
			c.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
			if(c.before(Calendar.getInstance()))
				c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
			while(c.get(Calendar.DAY_OF_WEEK) != daySelected + 1) {
				c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
			}
		}else {
			switch(daySelected) {
			case 7:
				// Every day
				c.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
				if(c.before(Calendar.getInstance()))
					c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
				break;
			default:
				System.err.print("?? calculdateDateAndTimeNext ??");
				JOptionPane.showMessageDialog(null, "Oops! An error occurred: calculateDateAndTimeNext invalid day selected", "An error occurred", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		return c;
	}
	
	private static void addB(StringBuffer buffer, long n, String name) {
		if(n == 0)
			return;
		if(buffer.length() > 0)
			buffer.append(",");
		buffer.append(" ");
		buffer.append(n).append(" ").append(name);
		if(n > 1) 
			buffer.append("s");
		
	}
	private static String format(long milliseconds)
	{
	   StringBuffer buffer = new StringBuffer();
	   long days, hours, minutes, seconds;
	   days = milliseconds / MILLISECONDS_PER_DAY;
	   milliseconds -= days * MILLISECONDS_PER_DAY;
	   hours = milliseconds / MILLISECONDS_PER_HOUR;
	   milliseconds -= hours * MILLISECONDS_PER_HOUR;
	   minutes = milliseconds / MILLISECONDS_PER_MINUTE;
	   milliseconds -= minutes * MILLISECONDS_PER_MINUTE;
	   seconds = milliseconds / MILLISECONDS_PER_SECOND;
	   milliseconds -= seconds * MILLISECONDS_PER_SECOND;
	   
	   addB(buffer, days   , "day"   );
	   addB(buffer, hours  , "hour"  );
	   addB(buffer, minutes, "minute");
	   addB(buffer, seconds, "second");
	   return buffer.toString();
	}
}
