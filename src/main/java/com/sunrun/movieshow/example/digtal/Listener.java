package com.sunrun.movieshow.example.digtal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class Listener extends MouseAdapter implements ActionListener {
	private String trainingPath = "data/knn/trainingDigit/";

	private Integer trainingMatrixDimension = 32;



	private Main mm;

	private Graphics2D g;

	private int[][] pixel = new int[trainingMatrixDimension][trainingMatrixDimension];

	// 下拉框，选择的数字
	private JComboBox<String> cbItem;

	private Knn knn;

	public Listener(Main mm, JComboBox<String> cbItem) {
		this.mm = mm;
		this.cbItem = cbItem;
		g = (Graphics2D) mm.getGraphics();
	}

	private int[][] sample = new int[trainingMatrixDimension][trainingMatrixDimension];

	public void actionPerformed(ActionEvent e) {
		// 存储当前判断正确的结果，将其进行存储
		if (e.getActionCommand().equals("存储样本")) {
			String selectedNumber = cbItem.getSelectedItem().toString();
			String fileName = selectedNumber + "_" + System.nanoTime();
			String absoluteFile = trainingPath + fileName+".txt";
			File file=new File(absoluteFile);
			try {
				if(!file.exists())
					file.createNewFile();
				FileWriter out = new FileWriter(file);
				for(int i=0;i < trainingMatrixDimension;i++) {
					for(int j=0;j < trainingMatrixDimension;j++) {
						out.write(pixel[i][j] + "");
					}
					out.write("\n");
				}
				out.flush();
				out.close();
			}catch(Exception e1) {
				e1.printStackTrace();
			}
			mm.repaint();
		} else if (e.getActionCommand().equals("识别")) {
			// K = 3
			knn = new Knn(3);
			File fileDir = new File(trainingPath);
			String[] fileList = fileDir.list();
			for(int i = 0;i < fileList.length; i++) {
				File file = new File(trainingPath + fileList[i]);
				String number = file.getName().substring(0, 1);
				try {
					List<String> lines = Files.readAllLines(Paths.get(trainingPath + fileList[i]));
					for (int j = 0;j < trainingMatrixDimension;j++) {
							String line = lines.get(j);
							for(int k = 0;k < trainingMatrixDimension; k++) {
								sample[j][k] = line.charAt(k) - '0';  //逐单位汉字/字母/数字读取
							}
					}
				} catch (Exception e1) {
                    e1.printStackTrace();
                }


				//开始进行 欧拉距离和KNN 运算；
				knn.sort(sample, pixel, trainingMatrixDimension, number);
			}
			JOptionPane.showMessageDialog(mm,"预测数字为：" + knn.predict());
			mm.repaint();

		}
		pixelReset();
	}
	
	private int x1, y1, x2, y2;


	public void pixelReset() {
		for (int i = 0; i < trainingMatrixDimension; i++) {
			for (int j = 0; j < trainingMatrixDimension; j++) {
				pixel[i][j] = 0;
			}
		}
	}

	public void mousePressed(MouseEvent e) {
		x1 = e.getX();
		y1 = e.getY();

	}


	public void mouseDragged(MouseEvent e) {
		g.setColor(Color.WHITE);
		// g.setStroke(new BasicStroke(20));
		x2 = e.getX();
		y2 = e.getY();
		g.fillRect(x2, y2, 20, 20);
		//怎么会产生倒置的情况
		for (int i = x2; i < x2 + 20; i++) {
			for (int j = y2; j < y2 + 20; j++) {
				pixel[Math.min(j/10,31)][Math.min(i/10,31)] = 1;
			}
		}



//		for (int i = x2; i < x2; i++) {
//			for (int j = y2; j < y2 + 20; j++) {
//				int pi = Math.min(j/10,31);
//				int pj = Math.min(i/10,31);
//				pixel[pi][pj] = 1;
//			}
//		}


	}

	public void mouseReleased(MouseEvent e) {
		System.out.println("=======================");
		for (int i = 0; i < pixel.length; i++){
			for (int j = 0; j < pixel.length; j++){
				if(pixel[i][j] == 1){
					System.out.print("* ");
				}else{
					System.out.print("0 ");
				}

			}
			System.out.println();
		}

	}
}