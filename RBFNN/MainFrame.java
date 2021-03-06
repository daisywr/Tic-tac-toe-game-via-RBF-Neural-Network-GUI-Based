/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RBFNN;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author wangru
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }
    private static ArrayList<instance> data=new ArrayList<instance>();
    private static int Hiddennum=0;
    private static int K=Hiddennum;
    private static double Dmax=0;
    private static ArrayList<instance> centers=new ArrayList<instance>();
    private static int[] confusion=new int[4];
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    public static ArrayList<instance> readFile(String fileName) {
		ArrayList<instance> result = new ArrayList<instance>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String str = br.readLine();
			while (str != null) {
				String[] sp = str.split(",");
				int len=sp.length;
				instance tmp = new instance(Arrays.copyOfRange(sp, 0, len-2),sp[len-1]);
				result.add(tmp);
				str = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();//note!
		}
		return result;
	}
    public static ArrayList<instance> pickdata(ArrayList<instance> data, double percent){
		ArrayList<instance> res=new ArrayList<instance>();
		ArrayList<Integer> rnum=new ArrayList<Integer>();
		int len=data.size();
		int sublen=(int) (len*percent);
		Map<Integer,instance> datamap= new HashMap<Integer, instance>();
		for(int i=0; i<len; i++){
			instance tmp=data.get(i);
			datamap.put(i, tmp);
		}//set data into hashmap in convenience of choosing data later
		Random rand=new Random();
		int count=0;
		while(count!=sublen){
			int n=rand.nextInt(sublen)+1;
			if(!rnum.contains(n)){
				count++;
				rnum.add(n);
			}
		}
		for(int i=0; i<sublen; i++){
			res.add(datamap.get(rnum.get(i)));
		}
		return res;
	}
    public static double Dmax(ArrayList<instance> centers){//calculate the largest
		//distance of the centers
		int len=centers.size(); 
		double maxG=-1;
		double maxL=-1;
		for(int i=0; i<len; i++){
			RealVector ci=MatrixUtils.createRealVector(centers.get(i).getnumFeature());	
			double dis=0;
			for(int j=0; j<len; j++){
				RealVector cj=MatrixUtils.createRealVector(centers.get(j).getnumFeature());
				dis=ci.getDistance(cj);
				if(dis>maxL)
					maxL=dis;
			}
			if(maxL>maxG)
				maxG=maxL;
		}
		return maxG;
	}
    public static RealMatrix RBFtrain(ArrayList<instance> trainingdata){
		RealMatrix W=MatrixUtils.createRealMatrix(K, 1);
		int n=K;//denotes the number of hidden layer neurons
		int m=trainingdata.size();
		Dmax=Dmax(centers);
		double[][] hiddenout= new double[n][m];
		for(int i=0; i<n; i++){//compute the output matrix of hidden neurons
			RealVector cen=MatrixUtils.createRealVector(centers.get(i).getnumFeature());
			double[] outCol=new double [m];
			for(int j=0; j<m; j++){
				double[] Xi=trainingdata.get(j).getnumFeature();
				RealVector Xii=MatrixUtils.createRealVector(Xi);
				outCol[j]=Math.exp(-Math.pow(Xii.subtract(cen).getNorm(),2)*K/(Dmax*Dmax));
			//employ the Guassian function to implement
			} 
			hiddenout[i]=outCol;
		}
		RealMatrix hiddenoutM=MatrixUtils.createRealMatrix(hiddenout);//n*m
		double[][] Yexp=new double [m][1];
		for(int j=0; j<m; j++){
			Yexp[j][0]=trainingdata.get(j).getnumcls();
		}
		RealMatrix YexpM=MatrixUtils.createRealMatrix(Yexp);//size: m*1
		Matrix hiddenM= new Matrix(hiddenoutM.transpose().getData());//change to another matrix library
		Matrix pseuInversHM = MoorePenroseInverse.pinv(hiddenM);
		RealMatrix pseuInversRM=MatrixUtils.createRealMatrix(pseuInversHM.getArray());
		W=pseuInversRM.multiply(YexpM);//size: n*1 or K*1
		return W;
	}
    public static int[] compare(RealMatrix YactM, RealMatrix YexpM){
		int[] confusion=new int[4];
		int len=YactM.getRowDimension();
		for(int i=0; i<len; i++){//classify the result into the category 
			if(YactM.getEntry(i, 0)>0.5)
				YactM.setEntry(i, 0, 1);
			else
				YactM.setEntry(i, 0, 0);
			if(YactM.getEntry(i, 0)==1&&YexpM.getEntry(i, 0)==1)
				confusion[0]++;
			if(YactM.getEntry(i, 0)==1&&YexpM.getEntry(i, 0)==0)
				confusion[1]++;
			if(YactM.getEntry(i, 0)==0&&YexpM.getEntry(i, 0)==1)
				confusion[2]++;
			if(YactM.getEntry(i, 0)==0&&YexpM.getEntry(i, 0)==0)
				confusion[3]++;
		}
		return confusion;
	}
    public static int[] RBFtest(RealMatrix W, ArrayList<instance> testingdata, double accuracy){
	//return four elements in the confusion Matrix
		int n=K;//denotes the number of hidden layer neurons
		int m=testingdata.size();
		double[][] hiddenout= new double[n][m];
		int[] confusion=new int[4];
		for(int i=0; i<K; i++){//compute the output matrix of hidden neurons
			RealVector cen=MatrixUtils.createRealVector(centers.get(i).getnumFeature());
			double[] outCol=new double [m];
			for(int j=0; j<m; j++){
				double[] Xi=testingdata.get(j).getnumFeature();
				RealVector Xii=MatrixUtils.createRealVector(Xi);
				outCol[j]=Math.exp(-Math.pow(Xii.subtract(cen).getNorm(),2)*K/(Dmax*Dmax));
			//employ the Guassian function to implement
			} 
			hiddenout[i]=outCol;
		}
		RealMatrix hiddenoutM=MatrixUtils.createRealMatrix(hiddenout);//n*m
		RealMatrix YactM=hiddenoutM.transpose().multiply(W);
		double[][] Yexp=new double [m][1];
		for(int j=0; j<m; j++){
			Yexp[j][0]=testingdata.get(j).getnumcls();
		}
		RealMatrix YexpM=MatrixUtils.createRealMatrix(Yexp);//size: m*1
		confusion=compare(YactM,YexpM);
		return confusion;
						
	}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        datapath = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        trainNum = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        testNum = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        hiddenNum = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        accuracy = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        weightsTable = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        confusionM = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 24)); // NOI18N
        jLabel1.setText("RBF Neural Network Implement Tic-Tac-Toe Game");

        jButton1.setText("Data");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        datapath.setText("jTextField1");
        datapath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datapathActionPerformed(evt);
            }
        });

        jButton2.setText("Execute");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel2.setText("Features and Weights");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        jLabel3.setText("Testing Confusion Matrix");

        trainNum.setText("jTextField1");

        jLabel4.setText("Training data number");

        jLabel5.setText("Testing data number");

        testNum.setText("jTextField1");
        testNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testNumActionPerformed(evt);
            }
        });

        jLabel6.setText("Input Number of Hidden Layer Neurons:");

        hiddenNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hiddenNumActionPerformed(evt);
            }
        });

        jLabel7.setText("Accuracy");

        accuracy.setText("jTextField1");
        accuracy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accuracyActionPerformed(evt);
            }
        });

        weightsTable.setColumns(20);
        weightsTable.setRows(5);
        jScrollPane3.setViewportView(weightsTable);

        confusionM.setColumns(20);
        confusionM.setRows(5);
        jScrollPane1.setViewportView(confusionM);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(datapath, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(140, 140, 140))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(hiddenNum, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))))))
                .addGap(62, 62, 62))
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2)
                            .addComponent(jLabel3))
                        .addGap(87, 87, 87))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(accuracy, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                            .addComponent(testNum)
                            .addComponent(trainNum))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(datapath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(hiddenNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(trainNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(testNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(accuracy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
        JFileChooser chooser= new JFileChooser(); 
        String filename=null;
        int option=chooser.showOpenDialog(MainFrame.this);
         if(option==JFileChooser.APPROVE_OPTION){
             File file= chooser.getSelectedFile();
             filename = file.getAbsolutePath();//get the absolute file path
             datapath.setText(filename);
         }
         data=readFile(filename);
    }//GEN-LAST:event_jButton1MouseClicked

    private void datapathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datapathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_datapathActionPerformed

    private void testNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testNumActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_testNumActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void hiddenNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hiddenNumActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hiddenNumActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        double len=data.size();
	double hNum=Double.parseDouble(hiddenNum.getText());
	double per=hNum/len;
        K=(int) hNum;
	centers=pickdata(data, per);
        ArrayList<instance> trainingdata=pickdata(data, 0.9);
	ArrayList<instance> testingdata=new ArrayList<instance>();
        for(int i=0; i<len; i++){
                    if(!trainingdata.contains(data.get(i)))
                        testingdata.add(data.get(i));
        }
        trainNum.setText(Integer.toString((int) ((int)len*0.9)));
        testNum.setText(Integer.toString((int) ((int)len*0.1)));
	RealMatrix W=RBFtrain(trainingdata);
        //String Ws=Arrays.toString(W.getData());
        String dataValues[][]= new String[W.getData().length+1][2];
        String table="Num       Weights"+"\n";
        for(int i=0; i<W.getData().length; i++){
            dataValues[i][0]="" + i;
            dataValues[i][1]=Double.toString(W.getData()[i][0]);
            table=table+dataValues[i][0]+"          "+dataValues[i][1]+"\n";
        }
        weightsTable.setText(table);
        float acc=0;
	confusion=RBFtest(W,testingdata,acc);
        String confs="             "+"positive"+"   "+"negative"+"\n";
        confs=confs+"positive"+"   "+Integer.toString(confusion[0])+"            "+Integer.toString(confusion[1])+"\n";
        confs=confs+"negative"+"   "+Integer.toString(confusion[2])+"            "+Integer.toString(confusion[3])+"\n";
        confusionM.setText(confs);
        acc=((float)confusion[0]+(float)confusion[3])/((float)confusion[0]+(float)confusion[1]+(float)confusion[2]+(float)confusion[3]);
        String accs= Float.toString(acc*100);
        accuracy.setText(accs+"%");
    }//GEN-LAST:event_jButton2MouseClicked

    private void accuracyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accuracyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accuracyActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accuracy;
    private javax.swing.JTextArea confusionM;
    private javax.swing.JTextField datapath;
    private javax.swing.JTextField hiddenNum;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField testNum;
    private javax.swing.JTextField trainNum;
    private javax.swing.JTextArea weightsTable;
    // End of variables declaration//GEN-END:variables
}
