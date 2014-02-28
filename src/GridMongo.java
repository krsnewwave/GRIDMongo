import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;

import java.awt.Color;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;

import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JProgressBar;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class GridMongo extends JFrame {
	private JTextField hostField;
	private JTextField portField;
	private JTextField inputFolderField;
	private JTextArea outputArea;
	private JToggleButton lockSettingsToggle;
	private JTextField dbnameField;
	private JCheckBox useLocalCheckbox;
	private JButton changeInputFolderButton;
	private JLabel fileCount;
	private JLabel totalSize;
	private long size;
	private volatile ArrayList<String> files;
	private JButton startTransferButton;
	private JButton stopTransferButton;
	private JProgressBar progressBar;
	private JLabel failedTransferCount;
	private JLabel queuedFilesCount;
	private MongoClient client;
	private JLabel transferredFiles;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GridMongo frame = new GridMongo();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	void startMongoConnection(){
		new ConnectionManager(this);
	}
	
	public void lockThings(){
		this.startTransferButton.setEnabled(false);
		this.changeInputFolderButton.setEnabled(false);
		lockSettingsToggle.setEnabled(false);
	}
	
	public void unlockThings(){
		this.startTransferButton.setEnabled(true);
		this.changeInputFolderButton.setEnabled(true);
		lockSettingsToggle.setEnabled(true);
	}
	
	public void clearOutputArea(){
		this.outputArea.setText("");
	}
	
	public void print(String text){
		this.outputArea.append(text + "\n");
	}
	
	/**
	 * 
	 * @return ArrayList of filenames fetched
	 */
	
	public ArrayList<String> getFiles(){
		return files;
	}
	
	public void clearFiles(){
		files.clear();
	}
	
	/**
	 * Update the progress bar on the UI
	 * @param value the value to set
	 */
	
	public void updateProgressBar(int value){
		this.progressBar.setValue(value);
	}
	
	/**
	 * Get the DB Name provided by the user, (in dbname textfield)
	 * @return the DB Name typed in the textfield
	 */
	
	public String getDBName(){
		if(!lockSettingsToggle.isSelected())
			return null;
		return this.dbnameField.getText();
	}
	
	/**
	 * 
	 * @return the MongoConnection created
	 */
	
	public MongoClient getClient(){ 
		return this.client;
	}
	
	/**
	 * Display the number of files still on queue to label 'Files On Queue'
	 * @param count > file count
	 */
	
	public void displayFilesOnQueue(int count){
		this.queuedFilesCount.setText(count + "");
	}
	
	/**
	 * Display the number of files successfully transferred on label 'Transferred Files'
	 * @param count > file count
	 */
	
	public void displayTransferredFiles(int count){
		this.transferredFiles.setText(count + "");
	}
	
	/**
	 * Create connection to db
	 */
	boolean connect(){
		try {
			client = new MongoClient(hostField.getText(), Integer.parseInt(portField.getText()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Please check your network settings", "Connection Error", JOptionPane.ERROR_MESSAGE);
		}
		
		try{
			BasicDBObject pingReply = client.getDB("admin").command(new BasicDBObject("ping", 1));
			if(pingReply.getInt("ok") == 1)
				return true;
		}
		catch(MongoException mongoe){
			JOptionPane.showMessageDialog(null, "Please check your network settings", "Connection Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "Please check your network settings", "Connection Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return false;
	}
	
	/**
	 * Create the frame.
	 */
	public GridMongo() {
		setLocationByPlatform(true);
		size = 0;
		files = new ArrayList<String>();
		setResizable(false);
		setTitle("Grid2Mongo");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 556);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(10, 11, 226, 170);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Host");
		lblNewLabel.setFont(new Font("Calibri Light", Font.BOLD, 16));
		lblNewLabel.setBounds(10, 11, 40, 14);
		panel.add(lblNewLabel);
		
		hostField = new JTextField();
		hostField.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		hostField.setBounds(60, 8, 150, 23);
		panel.add(hostField);
		hostField.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setFont(new Font("Calibri Light", Font.BOLD, 16));
		lblPort.setBounds(10, 42, 40, 14);
		panel.add(lblPort);
		
		portField = new JTextField(){
			 public void processKeyEvent(KeyEvent ev) {
	                char c = ev.getKeyChar();
	                try {
	                    // Ignore all non-printable characters. Just check the printable ones.
	                    if (c > 31 && c < 127) {
	                        Integer.parseInt(c + "");
	                    }
	                    super.processKeyEvent(ev);
	                }
	                catch (NumberFormatException nfe) {
	                    //ignore input
	                }
	            }
		};
		portField.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		portField.setColumns(10);
		portField.setBounds(60, 37, 90, 23);
		panel.add(portField);
		
		useLocalCheckbox = new JCheckBox("Use local settings");
		useLocalCheckbox.setFocusable(false);
		useLocalCheckbox.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		useLocalCheckbox.setBounds(60, 67, 140, 23);
		useLocalCheckbox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JCheckBox x = (JCheckBox) e.getSource();
				if(x.isSelected()){
					hostField.setText("localhost");
					hostField.setEditable(false);
					portField.setText("27017");
					portField.setEditable(false);
				}
				else{
					hostField.setText("");
					hostField.setEditable(true);
					portField.setText("");
					portField.setEditable(true);
				}
			}
		});
		panel.add(useLocalCheckbox);
		
		dbnameField = new JTextField();
		dbnameField.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		dbnameField.setColumns(10);
		dbnameField.setBounds(60, 98, 150, 23);
		panel.add(dbnameField);
		
		JLabel lblDb = new JLabel("DB");
		lblDb.setFont(new Font("Calibri Light", Font.BOLD, 16));
		lblDb.setBounds(10, 103, 40, 14);
		panel.add(lblDb);
		
		lockSettingsToggle = new JToggleButton("Lock Settings");
		lockSettingsToggle.setFocusable(false);
		lockSettingsToggle.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		lockSettingsToggle.setBounds(70, 132, 130, 27);
		lockSettingsToggle.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JToggleButton x = (JToggleButton) e.getSource();
				if(x.isSelected()){
					if(hostField.getText().trim().isEmpty()){
						x.setSelected(false);
						return;
					}
					if(portField.getText().trim().isEmpty()){
						x.setSelected(false);
						return;
					}
					if(dbnameField.getText().trim().isEmpty()){
						x.setSelected(false);
						return;
					}
					if(!useLocalCheckbox.isSelected()){
						portField.setEditable(false);
						hostField.setEditable(false);
						useLocalCheckbox.setEnabled(false);
					}
					useLocalCheckbox.setEnabled(false);
					dbnameField.setEditable(false);
				}
				else{
					if(!useLocalCheckbox.isSelected()){
						portField.setEditable(true);
						hostField.setEditable(true);
						useLocalCheckbox.setEnabled(true);
					}
					useLocalCheckbox.setEnabled(true);
					dbnameField.setEditable(true);
				}
			}
		});
		panel.add(lockSettingsToggle);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(246, 11, 438, 170);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblInputFolder = new JLabel("Input Folder");
		lblInputFolder.setFont(new Font("Calibri Light", Font.BOLD, 16));
		lblInputFolder.setBounds(10, 11, 90, 14);
		panel_1.add(lblInputFolder);
		
		inputFolderField = new JTextField();
		inputFolderField.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		inputFolderField.setColumns(10);
		inputFolderField.setBounds(10, 29, 319, 25);
		inputFolderField.setEditable(false);
		panel_1.add(inputFolderField);
		
		changeInputFolderButton = new JButton("Change");
		changeInputFolderButton.setFocusable(false);
		changeInputFolderButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				JFileChooser x = new JFileChooser(inputFolderField.getText());
				x.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int y = x.showDialog(null, "Select");
				if(y != 0)
					return;
				File f = x.getSelectedFile();
				inputFolderField.setText(f.getAbsolutePath());
				getFiles(inputFolderField.getText());
				
			}
		});
		changeInputFolderButton.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		changeInputFolderButton.setBounds(339, 28, 89, 27);
		panel_1.add(changeInputFolderButton);
		
		JLabel lblFiles = new JLabel("Fetched Files:");
		lblFiles.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		lblFiles.setBounds(10, 65, 110, 12);
		panel_1.add(lblFiles);
		
		fileCount = new JLabel("0");
		fileCount.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		fileCount.setBounds(130, 65, 75, 12);
		panel_1.add(fileCount);
		
		JLabel lblSize = new JLabel("Total Size: ");
		lblSize.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		lblSize.setBounds(10, 85, 110, 12);
		panel_1.add(lblSize);
		
		totalSize = new JLabel("0");
		totalSize.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		totalSize.setBounds(130, 85, 150, 12);
		panel_1.add(totalSize);
		
		JLabel lblFilesOnQueue = new JLabel("Files on queue:");
		lblFilesOnQueue.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		lblFilesOnQueue.setBounds(10, 105, 110, 12);
		panel_1.add(lblFilesOnQueue);
		
		queuedFilesCount = new JLabel("0");
		queuedFilesCount.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		queuedFilesCount.setBounds(130, 105, 75, 12);
		panel_1.add(queuedFilesCount);
		
		JLabel lblFailedTransfer = new JLabel("Failed Transfer:");
		lblFailedTransfer.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		lblFailedTransfer.setBounds(10, 125, 110, 12);
		panel_1.add(lblFailedTransfer);
		
		failedTransferCount = new JLabel("0");
		failedTransferCount.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		failedTransferCount.setBounds(130, 125, 75, 12);
		panel_1.add(failedTransferCount);
		
		JLabel lblTransferred = new JLabel("Transferred:");
		lblTransferred.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		lblTransferred.setBounds(10, 148, 110, 12);
		panel_1.add(lblTransferred);
		
		transferredFiles = new JLabel("0");
		transferredFiles.setFont(new Font("Calibri Light", Font.PLAIN, 14));
		transferredFiles.setBounds(130, 147, 75, 12);
		panel_1.add(transferredFiles);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setBounds(10, 192, 674, 273);
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 11, 654, 251);
		panel_2.add(scrollPane);
		
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		((DefaultCaret)outputArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane.setViewportView(outputArea);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_3.setBounds(10, 476, 674, 45);
		contentPane.add(panel_3);
		panel_3.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(10, 10, 484, 25);
		progressBar.setStringPainted(true);
		panel_3.add(progressBar);
		
		startTransferButton = new JButton("Start");
		startTransferButton.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		startTransferButton.setFocusable(false);
		startTransferButton.setBounds(504, 10, 75, 27);
		startTransferButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				//check if lock is on
				if(!lockSettingsToggle.isSelected()){
					JOptionPane.showMessageDialog(null, "Please lock settings first", "Transfer Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//check if there are files to transfer
				if(files.isEmpty()){
					JOptionPane.showMessageDialog(null, "No files to transfer", "Transfer Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//try to connect
				if(!connect()){
					JOptionPane.showMessageDialog(null, "Can't connect to server", "Connection Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				//check if the dbname provided exists
				List<String> dbs = client.getDatabaseNames();
				if(!dbs.contains(dbnameField.getText())){
					int x = JOptionPane.showConfirmDialog(null, "Database '" + dbnameField.getText() + "' doesn't exist.\n Create new database?", "Database Not Found", JOptionPane.YES_NO_OPTION);
					if(x != 0)
						return;
				}
				startMongoConnection();
			}
		});
		panel_3.add(startTransferButton);
		
		stopTransferButton = new JButton("Stop");
		stopTransferButton.setFont(new Font("Calibri Light", Font.PLAIN, 16));
		stopTransferButton.setFocusable(false);
		stopTransferButton.setBounds(589, 10, 75, 27);
		panel_3.add(stopTransferButton);
		
	}
	
	/**
	 * Get XML files in selected folder
	 */
	void getFiles(String path){
		String[] s = new File(path).list();
		files.clear();
		size = 0;
		for(String ss : s){
			if((path + "\\" + ss).endsWith(".xml") || (path + "\\" + ss).endsWith(".XML")){
				files.add(path + "\\" + ss);
				size += new File((path + "\\" + ss)).length();
			}
		}
		fileCount.setText(files.size() + "");
		totalSize.setText(size + " byte(s)");
	}
}