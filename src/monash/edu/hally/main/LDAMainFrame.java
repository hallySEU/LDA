package monash.edu.hally.main;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import monash.edu.hally.constant.ModelConstants;

/**
 * @author Hally
 */
public class LDAMainFrame implements ActionListener{
	
	private JFrame mainFrame;
	
	private JButton startButton,exitButton,defaultButton;
	private JTextField iterations,K,topNum,burn_in,saveStep;
	private JComboBox<Integer> samplingEquation;
//	private JTextArea messageArea;
	
	private ThreadGroup threadGroup=new ThreadGroup(ModelConstants.THREADGROUP_NAME);
	
	
	//程序入口
	public static void main(String[] args) {
		new LDAMainFrame();
	}
	
	public LDAMainFrame()
	{
		initFrame();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource()==startButton){
			if(!parametersCheck()) return;
			if(threadGroup.activeCount()==0){	//如果指定的线程组没有激活状态的线程，
												//那么分析线程不在运行，则创建一个分析线程。
				Thread modelThread= new Thread(threadGroup, new ModelThread(setParameters()));
				modelThread.start();
			}	
		}
		else if(e.getSource()==exitButton)
			System.exit(0);
		else
			setDefaultParameters();
			
	}
	
	/**
	 * 作用：判断参数输入是否为空，是否合法。
	 */
	public boolean parametersCheck()
	{
		if(K.getText().equals("")||topNum.getText().equals("")||iterations.getText().equals("")
			||burn_in.getText().equals("")||saveStep.getText().equals(""))
			JOptionPane.showMessageDialog(mainFrame, "Please input parameters completely.");
		else if(Integer.valueOf(iterations.getText().trim()) <
				Integer.valueOf(burn_in.getText().trim())+Integer.valueOf(saveStep.getText().trim()))
			JOptionPane.showMessageDialog(mainFrame, "Please make sure that "
					+ "Iterations should bigger than the sum of Burn-in and SaveStep.");
		else
			 return true;
		
		return false;
	}
	
	/**
	 * 作用：设置模型参数
	 */
	public ModelParameters setParameters()
	{
		ModelParameters modelParameters=new ModelParameters();
		modelParameters.setK(Integer.valueOf(K.getText().trim()));
		modelParameters.setTopNum(Integer.valueOf(topNum.getText().trim()));
		modelParameters.setIterations(Integer.valueOf(iterations.getText().trim()));
		modelParameters.setBurn_in(Integer.valueOf(burn_in.getText().trim()));
		modelParameters.setSaveStep(Integer.valueOf(saveStep.getText().trim()));
		modelParameters.setSamplingEquation(Integer.valueOf(samplingEquation.getSelectedItem().toString()));
		
		return modelParameters;
	}
	
	/**
	 * 作用：设置模型默认参数
	 */
	public void setDefaultParameters()
	{
		iterations.setText("100");
		K.setText("10");
		topNum.setText("10");
		burn_in.setText("80");
		saveStep.setText("10");
		samplingEquation.setSelectedIndex(0);
	}

	
	public void initFrame()
	{
		mainFrame=new JFrame("LDA topic model");
			
		JPanel bottomPanel=new JPanel();
		startButton=new JButton("Start");
		startButton.addActionListener(this);
		exitButton=new JButton("Exit");
		exitButton.addActionListener(this);
		bottomPanel.add(startButton);
		bottomPanel.add(exitButton);
		
		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new GridLayout(6,2,10,10));
		mainPanel.setBorder(BorderFactory.createTitledBorder(null, "Parameters setting",
				TitledBorder.CENTER , TitledBorder.TOP,new Font("", Font.BOLD, 15)));
		JLabel l1=new JLabel("K (Number of topics):",JLabel.CENTER);
		JLabel l2=new JLabel("Top number:",JLabel.CENTER);
		JLabel l3=new JLabel("Iterations:",JLabel.CENTER);
		JLabel l4=new JLabel("Burn_in:",JLabel.CENTER);
		JLabel l5=new JLabel("Savestep:",JLabel.CENTER);
		JLabel l6=new JLabel("Gibbs sampling equation:",JLabel.CENTER);
		iterations=new JTextField(15);
		topNum=new JTextField(15);
		K=new JTextField(15);
		burn_in=new JTextField(15);
		saveStep=new JTextField(15);
		defaultButton=new JButton("Default");
		defaultButton.addActionListener(this);
		samplingEquation=new JComboBox<Integer>(new Integer[]{1,2});
		mainPanel.add(l1); mainPanel.add(K);
		mainPanel.add(l2); mainPanel.add(topNum);
		mainPanel.add(l3); mainPanel.add(iterations);
		mainPanel.add(l4); mainPanel.add(burn_in);
		mainPanel.add(l5); mainPanel.add(saveStep);
		mainPanel.add(l6); mainPanel.add(samplingEquation);
		bottomPanel.add(defaultButton);
		
		mainFrame.add(mainPanel);
		mainFrame.add(bottomPanel,BorderLayout.SOUTH);
		mainFrame.setSize(350, 350);
		mainFrame.setVisible(true);
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null); //show in the center of screen
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
