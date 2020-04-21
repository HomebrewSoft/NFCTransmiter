package dev.homebrewsoft.nfctransmiter;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class GUI {
	
	private static JButton btn;
	private static JTextArea txt;
	private static Thread process;
	
	public static void main(String args[]) {
		JFrame frame = new JFrame("Enviar URL de factura por NFC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(200, 200, 600, 300);
		
		GridLayout grid = new GridLayout(0, 1);
		
		btn = new JButton("Freetix");
		txt = new JTextArea();
		
		JScrollPane scroll = new JScrollPane(txt);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
	        public void adjustmentValueChanged(AdjustmentEvent e) {  
	            e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
	        }
	    });
		
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btn.setEnabled(false);
				process = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Main.process(txt);
						btn.setEnabled(true);
					}
					
				});
				process.start();
			}
			
		});
		
		txt.setEditable(false);
		
		frame.add(btn);
		frame.add(scroll);
		
		frame.setLayout(grid);
		frame.setVisible(true);
	}
}
