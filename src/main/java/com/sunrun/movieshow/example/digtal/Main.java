package com.sunrun.movieshow.example.digtal;

import javax.swing.*;
import java.awt.*;

public class Main extends JPanel{

	public static void main(String[] args) {
		Main mm = new Main();
		mm.init();
	}

	public void init() {
		JFrame jf=new JFrame();
		jf.setLayout(new FlowLayout());
		jf.setResizable(false);
		jf.setPreferredSize(null);
		jf.setDefaultCloseOperation(3);
		jf.setSize(500,600);   //窗体的大小也会影响精度；
		jf.setLocationRelativeTo(null);
		
		JButton button1 = new JButton("识别");
		JButton button2 = new JButton("学习样本");
		
		button1.setPreferredSize(new Dimension(100, 30));
		button2.setPreferredSize(new Dimension(100, 30));
		String[] itemArray= {"0","1","2","3","4","5","6","7","8","9"};
		JComboBox<String> cbItem = new JComboBox<String>(itemArray);
		this.setBackground(Color.black);
		
		//JPanel 设置大小应该用setPreferredSize
		this.setPreferredSize(new Dimension(400,400));
		
		jf.add(button1);
		jf.add(button2);
		jf.add(cbItem);
		jf.add(this);
		jf.setVisible(true);
		Listener listener = new Listener(this,cbItem);
		
		button1.addActionListener(listener);
		button2.addActionListener(listener);

		//鼠标监听器分了两种，如果要监听移动动作，一定是加上motionlistener;
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}

}