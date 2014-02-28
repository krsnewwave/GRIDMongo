import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.DropMode;

import com.mongodb.MongoClient;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import org.json.JSONException;
import javax.swing.ScrollPaneConstants;


public class ReadFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ReadUtility util;
	private MongoClient client;
	private ArrayList<String> results;
	private JPanel contentPane;
	private String[] outputTypes = new String[]{"PackageDataSet", "PackageFamily", "File", "Package", "ParentFile", "Source"};
	private String[][] inputs = new String[][]{
			{"Object ID", "PackageFamily ID", "File ID", "Package ID", "ParentFile ID", "Source ID", "Processing Start - Date"},
			{"Object ID", "PackageDataSet ID", "Package ID", "Vendor Name"},
			{"Object ID", "PackageDataSet ID", "ParentFile ID", "Tag", "md5", "sha1", "First Seen - Date", "Last Processed - Date", "Last Retrieved - Date"},
			{"Object ID", "PackageDataSet ID", "PackageFamily ID", "ParentFile ID", "Source ID", "Tag", "Vendor Name", "PackageFamily Name", "Name Relation"},
			{"Object ID", "PackageDataSet ID", "File ID", "Package ID", "Tag"},
			{"Object ID", "PackageDataSet ID", "Package ID", "sha1", "Last Modified - Date"}
	};
	private JComboBox<String> outputTypes_dropDown;
	private JComboBox<String> inputTypes_dropDown;
	private JButton viewAll_button;
	private JButton viewOne_button;
	private JButton search_button;
	private JTextArea output_txtarea;
	private JTextField searchValue_txtfield;
	private JTextField dbname_txtfield;
	private JToggleButton lockSettings_toggleButton;
	private JTextField host_txtfield;
	private JTextField port_txtfield;
	private JLabel result_label;
	private JCheckBox useLocalSettings_checkBox;
	private JTextField viewNumber;
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ReadFrame frame = new ReadFrame();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public ReadFrame() {
		results = new ArrayList<>();
		setTitle("Read");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 510);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel.setBounds(10, 188, 241, 166);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel label = new JLabel("Find");
		label.setBounds(10, 16, 40, 14);
		panel.add(label);
		label.setFont(new Font("Calibri Light", Font.BOLD, 15));
		
		outputTypes_dropDown = new JComboBox<String>();
		outputTypes_dropDown.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		
		outputTypes_dropDown.setBounds(60, 11, 171, 25);
		outputTypes_dropDown.setModel(new DefaultComboBoxModel<>(outputTypes));
		outputTypes_dropDown.setFocusable(false);
		outputTypes_dropDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				inputTypes_dropDown.setModel(new DefaultComboBoxModel<>(inputs[cb.getSelectedIndex()]));
			}
		});
		panel.add(outputTypes_dropDown);
		
		JLabel lblBy = new JLabel("Using");
		lblBy.setBounds(10, 45, 40, 14);
		panel.add(lblBy);
		lblBy.setFont(new Font("Calibri Light", Font.BOLD, 15));
		
		inputTypes_dropDown = new JComboBox<String>();
		inputTypes_dropDown.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		inputTypes_dropDown.setBounds(60, 40, 171, 25);
		inputTypes_dropDown.setFocusable(false);
		inputTypes_dropDown.setModel(new DefaultComboBoxModel<>(inputs[0]));
		panel.add(inputTypes_dropDown);
		
		JLabel lblValue = new JLabel("Value");
		lblValue.setFont(new Font("Calibri Light", Font.BOLD, 15));
		lblValue.setBounds(10, 69, 40, 14);
		panel.add(lblValue);
		
		searchValue_txtfield = new JTextField();
		searchValue_txtfield.setHorizontalAlignment(SwingConstants.CENTER);
		searchValue_txtfield.setFont(new Font("Calibri", Font.PLAIN, 15));
		searchValue_txtfield.setBounds(32, 94, 170, 23);
		searchValue_txtfield.setColumns(10);
		panel.add(searchValue_txtfield);
		
		search_button = new JButton("Search");
		search_button.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		search_button.setBounds(69, 128, 100, 25);
		search_button.setFocusable(false);
		search_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(!lockSettings_toggleButton.isSelected()){
					JOptionPane.showMessageDialog(null, "Please lock settings first", "Search Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(searchValue_txtfield.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(null, "Please provide a search value", "Search Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				results.clear();
				output_txtarea.setText("");
				
				try {
					client = new MongoClient(host_txtfield.getText(), Integer.parseInt(port_txtfield.getText()));
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					output_txtarea.setText(e1.getMessage());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					output_txtarea.setText(e1.getMessage());
				}
				
				int outputType = 0, inputType;
				
				outputType = 	outputTypes_dropDown.getSelectedItem().equals("PackageDataSet")? ReadUtility.OUTPUT_PACKAGEDATASET : 
								outputTypes_dropDown.getSelectedItem().equals("PackageFamily")? ReadUtility.OUTPUT_PACKAGEFAMILY :
								outputTypes_dropDown.getSelectedItem().equals("File")? ReadUtility.OUTPUT_FILE :
								outputTypes_dropDown.getSelectedItem().equals("Package")? ReadUtility.OUTPUT_PACKAGE :
								outputTypes_dropDown.getSelectedItem().equals("ParentFile")? ReadUtility.OUTPUT_PARENTFILE :
								outputTypes_dropDown.getSelectedItem().equals("Source")? ReadUtility.OUTPUT_SOURCE : -1;
				
				inputType = 	inputTypes_dropDown.getSelectedItem().equals("Object ID")? ReadUtility.INPUT_OBJECTID :
								inputTypes_dropDown.getSelectedItem().equals("PackageDataSet ID")? ReadUtility.INPUT_PACKAGEDATASETID :
								inputTypes_dropDown.getSelectedItem().equals("PackageFamily ID")? ReadUtility.INPUT_PACKAGEFAMILYID :
								inputTypes_dropDown.getSelectedItem().equals("File ID")? ReadUtility.INPUT_FILEID :
								inputTypes_dropDown.getSelectedItem().equals("Package ID")? ReadUtility.INPUT_PACKAGEID :
								inputTypes_dropDown.getSelectedItem().equals("ParentFile ID")? ReadUtility.INPUT_PARENTFILEID :
								inputTypes_dropDown.getSelectedItem().equals("Source ID")? ReadUtility.INPUT_SOURCEID :
								inputTypes_dropDown.getSelectedItem().equals("Processing Start - Date")? ReadUtility.INPUT_PROCESSINGSTARTED :
								inputTypes_dropDown.getSelectedItem().equals("Vendor Name")? ReadUtility.INPUT_VENDORNAME :
								inputTypes_dropDown.getSelectedItem().equals("Tag")? ReadUtility.INPUT_TAG :
								inputTypes_dropDown.getSelectedItem().equals("md5")? ReadUtility.INPUT_MD5 :
								inputTypes_dropDown.getSelectedItem().equals("sha1")? ReadUtility.INPUT_SHA1 :
								inputTypes_dropDown.getSelectedItem().equals("First Seen - Date")? ReadUtility.INPUT_FIRSTSEEN :
								inputTypes_dropDown.getSelectedItem().equals("Last Processed - Date")? ReadUtility.INPUT_LASTPROCESSED :
								inputTypes_dropDown.getSelectedItem().equals("Last Retrieved - Date")? ReadUtility.INPUT_LASTRETRIEVED :
								inputTypes_dropDown.getSelectedItem().equals("PackageFamily Name")? ReadUtility.INPUT_FAMILYNAME :
								inputTypes_dropDown.getSelectedItem().equals("Name Relation")? ReadUtility.INPUT_NAMERELATION :
								inputTypes_dropDown.getSelectedItem().equals("Last Modified - Date")? ReadUtility.INPUT_LASTMODIFIED : -1;
				
				util = new ReadUtility(client);
				
				util.setDB(dbname_txtfield.getText());
				
				results = util.search(outputType, inputType, searchValue_txtfield.getText());
				
				result_label.setText(results.size() + "");
				JOptionPane.showMessageDialog(null, results.size());
				
			}
		});
		panel.add(search_button);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setBounds(261, 11, 713, 454);
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_2.setBounds(10, 11, 693, 432);
		panel_2.add(scrollPane_2);
		
		output_txtarea = new JTextArea();
		output_txtarea.setDropMode(DropMode.INSERT);
		output_txtarea.setEditable(false);
		scrollPane_2.setViewportView(output_txtarea);
		output_txtarea.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		output_txtarea.setLineWrap(true);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel_3.setBounds(10, 11, 241, 166);
		contentPane.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblHost = new JLabel("Host");
		lblHost.setFont(new Font("Calibri Light", Font.BOLD, 15));
		lblHost.setBounds(10, 10, 35, 14);
		panel_3.add(lblHost);
		
		host_txtfield = new JTextField();
		host_txtfield.setHorizontalAlignment(SwingConstants.CENTER);
		host_txtfield.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		host_txtfield.setBounds(50, 7, 180, 23);
		host_txtfield.setDisabledTextColor(Color.RED);
		panel_3.add(host_txtfield);
		host_txtfield.setColumns(10);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setFont(new Font("Calibri Light", Font.BOLD, 15));
		lblPort.setBounds(10, 45, 35, 14);
		panel_3.add(lblPort);
		
		port_txtfield = new JTextField();
		port_txtfield.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		port_txtfield.setColumns(10);
		port_txtfield.setHorizontalAlignment(JTextField.CENTER);
		port_txtfield.setDisabledTextColor(Color.RED);
		port_txtfield.setBounds(50, 41, 60, 23);
		panel_3.add(port_txtfield);
		
		JLabel lblDb = new JLabel("DB");
		lblDb.setFont(new Font("Calibri Light", Font.BOLD, 15));
		lblDb.setBounds(10, 104, 35, 14);
		panel_3.add(lblDb);
		
		dbname_txtfield = new JTextField();
		dbname_txtfield.setHorizontalAlignment(SwingConstants.CENTER);
		dbname_txtfield.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		dbname_txtfield.setColumns(10);
		dbname_txtfield.setDisabledTextColor(Color.RED);
		dbname_txtfield.setBounds(50, 100, 180, 23);
		panel_3.add(dbname_txtfield);
		
		lockSettings_toggleButton = new JToggleButton("Lock");
		lockSettings_toggleButton.setFont(new Font("Calibri Light", Font.BOLD, 15));
		lockSettings_toggleButton.setBounds(50, 134, 115, 25);
		lockSettings_toggleButton.setFocusable(false);
		lockSettings_toggleButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JToggleButton x = (JToggleButton) e.getSource();
				if(x.isSelected()){
					if(host_txtfield.getText().trim().isEmpty()){
						JOptionPane.showMessageDialog(null, "Please provide a hostname", "Lock Settings", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(port_txtfield.getText().trim().isEmpty()){
						JOptionPane.showMessageDialog(null, "Please provide a port", "Lock Settings", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(dbname_txtfield.getText().trim().isEmpty()){
						JOptionPane.showMessageDialog(null, "Please provide a database name", "Lock Settings", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					host_txtfield.setEnabled(false);
					port_txtfield.setEnabled(false);
					dbname_txtfield.setEnabled(false);
					useLocalSettings_checkBox.setEnabled(false);
				}
				else{
					if(!useLocalSettings_checkBox.isSelected()){
						host_txtfield.setEnabled(true);
						port_txtfield.setEnabled(true);
					}
					dbname_txtfield.setEnabled(true);
					useLocalSettings_checkBox.setEnabled(true);
				}
			}
		});
		panel_3.add(lockSettings_toggleButton);
		
		useLocalSettings_checkBox = new JCheckBox("Use local setttings");
		useLocalSettings_checkBox.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		useLocalSettings_checkBox.setBounds(50, 71, 133, 23);
		useLocalSettings_checkBox.setFocusable(false);
		useLocalSettings_checkBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JCheckBox x = (JCheckBox) e.getSource();
				if(x.isSelected()){
					host_txtfield.setText("localhost");
					port_txtfield.setText("27017");
					host_txtfield.setEnabled(false);
					port_txtfield.setEnabled(false);
				}
				else{
					host_txtfield.setText("");
					port_txtfield.setText("");
					host_txtfield.setEnabled(true);
					port_txtfield.setEnabled(true);
					host_txtfield.requestFocus();
				}
			}
		});
		panel_3.add(useLocalSettings_checkBox);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		panel_1.setBounds(10, 365, 241, 100);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		viewAll_button = new JButton("View All");
		viewAll_button.setBounds(51, 36, 133, 25);
		viewAll_button.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		viewAll_button.setFocusable(false);
		viewAll_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(results.isEmpty())
					return;
				String collectionName = 	outputTypes_dropDown.getSelectedItem().equals("PackageDataSet")? "packageDataSets" : 
											outputTypes_dropDown.getSelectedItem().equals("PackageFamily")? "packageFamilies" :
											outputTypes_dropDown.getSelectedItem().equals("File")? "files" :
											outputTypes_dropDown.getSelectedItem().equals("Package")? "packages" :
											outputTypes_dropDown.getSelectedItem().equals("ParentFile")? "parentFiles" :
											outputTypes_dropDown.getSelectedItem().equals("Source")? "sources" : "";
				new Thread(new Printer(results, client, dbname_txtfield.getText(), output_txtarea, collectionName)).start();
			}
		});
		panel_1.add(viewAll_button);
		
		viewOne_button = new JButton("View");
		viewOne_button.setBounds(114, 64, 70, 25);
		viewOne_button.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		viewOne_button.setFocusable(false);
		viewOne_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				if(results.isEmpty())
					return;
				
				if(viewNumber.getText().trim().isEmpty())
					return;
				
				int num = 0;
				
				try{
					num = Integer.parseInt(viewNumber.getText());
				}
				catch(NumberFormatException nfe){
					JOptionPane.showMessageDialog(null, "Invalid Number");
					return;
				}
				
				if(num <= 0){
					JOptionPane.showMessageDialog(null, "Invalid Number");
					return;
				}
				if(num > results.size()){
					JOptionPane.showMessageDialog(null, "Invalid Number");
					return;
				}
				
				String collectionName = 	outputTypes_dropDown.getSelectedItem().equals("PackageDataSet")? "packageDataSets" : 
					outputTypes_dropDown.getSelectedItem().equals("PackageFamily")? "packageFamilies" :
					outputTypes_dropDown.getSelectedItem().equals("File")? "files" :
					outputTypes_dropDown.getSelectedItem().equals("Package")? "packages" :
					outputTypes_dropDown.getSelectedItem().equals("ParentFile")? "parentFiles" :
					outputTypes_dropDown.getSelectedItem().equals("Source")? "sources" : "";
				
				new Thread(new Printer(results.get(num - 1), client, dbname_txtfield.getText(), output_txtarea, collectionName)).start();
				
			}
		});
		panel_1.add(viewOne_button);
		
		JLabel lblResult = new JLabel("Result:");
		lblResult.setBounds(64, 11, 59, 14);
		panel_1.add(lblResult);
		lblResult.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		
		result_label = new JLabel("0");
		result_label.setBounds(133, 11, 41, 14);
		panel_1.add(result_label);
		result_label.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		
		viewNumber = new JTextField();
		viewNumber.setHorizontalAlignment(SwingConstants.CENTER);
		viewNumber.setFont(new Font("Calibri Light", Font.PLAIN, 15));
		viewNumber.setDisabledTextColor(Color.RED);
		viewNumber.setColumns(10);
		viewNumber.setBounds(51, 65, 60, 23);
		panel_1.add(viewNumber);
	}
}

class Printer implements Runnable{

	private JTextArea outputArea;
	private MongoClient client;
	private ArrayList<String> ids;
	private String collectionName, dbname, id = "";
	private ReadUtility util;
	
	public Printer(ArrayList<String> ids, MongoClient client, String dbname, JTextArea outputArea, String collectionName){
		this.outputArea = outputArea;
		this.client = client;
		this.ids = ids;
		this.dbname = dbname;
		this.collectionName = collectionName;
	}
	
	public Printer(String id, MongoClient client, String dbname, JTextArea outputArea, String collectionName){
		this.outputArea = outputArea;
		this.client = client;
		this.id = id;
		this.dbname = dbname;
		this.collectionName = collectionName;
		ids = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		util = new ReadUtility(client);
		util.setDB(dbname);
		// TODO Auto-generated method stub
		outputArea.setText("");
		
		if(ids.isEmpty() && !id.equals("")){
			try {
				outputArea.append(util.getJSON(id, collectionName).toString(3) + "\n");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		for(int a = 0; a < ids.size(); a++){
			try {
				outputArea.append("----- " + (a + 1) + " -----\n");
				outputArea.append(util.getJSON(ids.get(a), collectionName).toString(3) + "\n");
				outputArea.append("----- end of " + (a + 1) + " -----\n");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
}