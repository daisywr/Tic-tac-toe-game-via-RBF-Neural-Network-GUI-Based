/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RBFNN;

/**
 *
 * @author wangru
 */
public class instance {
    
	String[] feature;
	String cls;
	double[] numFeature;
	double numcls;
	public instance(String[] feature, String cls)
	{
		this.cls=cls;
		this.feature=feature.clone();
		double[] numF=new double[feature.length];
		for(int i=0; i<feature.length; i++){
			if(this.feature[i].equals("x"))
				numF[i]=1;
			else if(this.feature[i].equals("o"))
				numF[i]=-1;
			else
				numF[i]=0;
		}
		this.numFeature=numF;
		if(this.cls.equals("positive"))
			this.numcls=1;
		else
			this.numcls=0;
	}
	public void setfeature(String[] feature) {this.feature=feature.clone();}
	public void setcls(String cls) {this.cls=cls;}
	public String[] getfeature() {return this.feature;}
	public String getcls() {return this.cls;}
	public double getnumcls(){ return this.numcls;	}
	public double[] getnumFeature(){ return this.numFeature;}
	
}
