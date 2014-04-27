package org.twenty20twenty;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Twenty20Twenty {

	static TrayIcon trayIcon = null;

	static TimerTask reminder = null;
	static Timer timer = null;

	static long interval = 20 * 1000 * 60;
	
	protected static RandomAccessFile lockFile = null;
	
	static final String aboutMessage = "20-20-20 Rule "
			+ "For The Eye\r\n"
			+ "______________________________________________________________\r\n"
			+ "Follow this rule to reduce eye strain when working in front of a computer.\r\n"
			+ "Every 20 minutes look at something 20 feet away for "
			+ "20 seconds.\r\n"
			+ "______________________________________________________________"
			+

			"\r\nAnto Paul";

	public static void main(String[] args) {
		
		if(isAlreadyRunning()) {
			System.out.println("Already running. Exiting...");
			System.exit(0);
		}
		createLock();
		
		/* Use an appropriate Look and Feel */
		try {
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		/* Turn off metal's use of bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		// Schedule a job for the event-dispatching thread:
		// adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		setTimer();
	}

	private static void createAndShowGUI() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final Image image = createImage("/images/eye.gif", "20-20-20 tray icon");
		trayIcon = new TrayIcon(image);
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About...");

		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		trayIcon.setToolTip("20-20-20 Rule For The Eye");
		
		ActionListener aboutListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Icon icon = new ImageIcon(image);
				JOptionPane.showMessageDialog(null, aboutMessage, 
						"About 20-20-20", JOptionPane.PLAIN_MESSAGE, icon);
			}
        };

		aboutItem.addActionListener(aboutListener);

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				releaseLock();
				tray.remove(trayIcon);
				timer.cancel();
				//System.exit(0);
			}
		});
		
		trayIcon.addActionListener(aboutListener);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}
	}

	// Obtain the image URL
	protected static Image createImage(String path, String description) {
		URL imageURL = Twenty20Twenty.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	protected static void setTimer() {

		reminder = new TimerTask() {

			@Override
			public void run() {
				trayIcon.displayMessage("20-20-20", "It's 20-20-20 time",
						TrayIcon.MessageType.INFO);
			}
		};

		timer = new Timer();
		timer.schedule(reminder, interval, interval);
	}
	
	protected static FileLock createLock() {
		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "Twenty2020.tmp");
		file.deleteOnExit();
		
		FileChannel fileChannel = null;
		FileLock lock = null;
		
		try {
			lockFile = new RandomAccessFile(file,"rw");
			fileChannel = lockFile.getChannel();
			lock = fileChannel.tryLock();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lock;
	}
	
	protected static void releaseLock() {
		if(lockFile != null) {
			try {
				lockFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected static boolean isAlreadyRunning() throws RuntimeException {

		String temp = System.getProperty("java.io.tmpdir");
		File file = new File(temp, "Twenty2020.tmp");
		file.deleteOnExit();
		FileChannel fileChannel = null;
		
		try {
			lockFile = new RandomAccessFile(file,"rw");
			fileChannel = lockFile.getChannel();
			FileLock lock = fileChannel.tryLock();
			
			if(lock == null) {
				return true;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fileChannel != null) {
				try {
					fileChannel.close();
					lockFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return false;
		
	}
}