package monash.edu.hally.main;


import monash.edu.hally.util.Document;
import monash.edu.hally.util.Documents;
import monash.edu.hally.util.FilesUtil;

public class LDAModel extends Thread{

	private double alpha,beta;	//����(dirichlet�ֲ������Ĳ���)
	private int M,K,V;	//�ֱ��ʾ�ĵ������ĵ��ĸ���������������ֵ��еĴʻ����
	private double[][] phi;	//����-�ʻ�ֲ�  ά�� K*V
	private double[][] theta;	//�ĵ�-����ֲ�  ά�� M*K
	private int[][] nmk;	//ÿһ��Ϊ���ض��ĵ��µ��ض�������ֵĴ���   ά�� M*K
	private int[][] nkt;	//ÿһ��Ϊ���ض������µ��ض��ʻ���ֵĴ���   ά�� K*V
	private int[] nktSum;	//ÿһ��Ϊ���ض������µ����дʻ���ֵĴ�����
	private int[] nmkSum;	//ÿһ��Ϊ���ض��ĵ��µ�����������ֵĴ�����
	
	private int[][] z;		//ÿһ��Ϊ���ض��ĵ��µ��ض��ʻ������
	
	private int iterations;	//��������
	private int burn_in;	//burn-in ʱ��
	private int saveStep;	//burn-in ����ÿsaveStep�ε�������һ�ν��
	private int topNum;	//��ʾ�����¸�����ߵ�ǰtopNum����
	
	private Documents documents;	//�ĵ���
	private ModelParameters modelParameters;	//ģ�Ͳ���
//	private int saveTime=0;	//���������������
	
	
	
	public LDAModel(Documents documents, ModelParameters modelParameters)
	{
		this.documents=documents;
		this.modelParameters=modelParameters;
		setModelParameters();
	}
	
	/**
	 * ���ã�������ҪԤ��ָ���Ĳ���
	 */
	public void setModelParameters()
	{
//		System.out.println("Read model parameters.");//��һ�ֶ�ȡ�����ķ�ʽ
//		ModelParameters modelParameters=FilesUtil.readParametersFile();
	
		K=modelParameters.getK();
		alpha=50/K;	//һ��Ϊ 50/K
		beta=0.1;	//һ��Ϊ 0.1
		iterations=modelParameters.getIterations();
		burn_in=modelParameters.getBurn_in();
		saveStep=modelParameters.getSaveStep();
		topNum=modelParameters.getTopNum();
	}
	
	/**
	 * ���ã���ʼ��ģ��
	 * 1.��ʼ��ģ�Ͳ�����������Ҫѧϰ���ĵ����õ���
	 * 2.���ĵ��еĴʻ������������
	 */
	public void initialiseModel()
	{
		System.out.println("Model begins learning.");
		
		M=documents.docs.size();
		V=documents.dictionary.size();
		
		phi=new double[K][V];
		theta=new double[M][K];
		nmk=new int[M][K];
		nkt=new int[K][V];
		nktSum=new int[K];
		nmkSum=new int[M];
		z=new int[M][];
		
		for (int m = 0; m < M; m++) {
			Document document=documents.docs.get(m);
			int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�
			z[m]=new int[Nm];
			for (int n = 0; n < Nm; n++) {
				int zmn=(int) (Math.random()*(K));	//�������
				z[m][n]=zmn;
				nmk[m][zmn]++;
				nkt[zmn][document.docWords[n]]++;
				nktSum[zmn]++;
				nmkSum[m]++;
			}
			
		}
	}
	
	/**
	 * ���ã�����Gibbs�����㷨�����ƶ�ģ�Ͳ���
	 */
	public void inferenceModel()
	{
		
		for (int currentIteration = 1; currentIteration <= iterations; currentIteration++) {
			System.out.println("Iteration "+currentIteration);
			if(currentIteration == iterations)
				saveLatentVariables();
			else if((currentIteration >= burn_in) && (currentIteration % saveStep==0))
				calLatentVariables(false);
			else
			{	//��ͣ�Ĳ�����ֱ������burn-inʱ��
				for (int m = 0; m < M; m++) {
					Document document=documents.docs.get(m);
					for (int n = 0; n < document.docWords.length; n++) {
						int newTopic=sampleTopic(m,n);
						z[m][n]=newTopic;
					}
				}
			}
		}
		System.out.println("Learn over!");
	}
	
	/**
	 * Gibbs�����㷨������ǰ�ʻ����·����µ�����
	 * @return �µ�����
	 */
	public int sampleTopic(int m,int n)
	{
		int termIndex=documents.docs.get(m).docWords[n];
		
		int oldTopic=z[m][n];
		nmk[m][oldTopic]--;
		nkt[oldTopic][termIndex]--;
		nktSum[oldTopic]--;
		nmkSum[m]--;
		
		double[] p=new double[K];
		
		for (int k = 0; k < K; k++) {
			if(modelParameters.getSamplingEquation()==1)//Gibbs������ʽ 1��Դ ���ģ�Parameter estimation for text analysis	
				p[k]=(nkt[k][termIndex]+beta)/(nktSum[k]+beta)*
						(nmk[m][k]+alpha)/(nmkSum[m]+alpha-1);
			else								//Gibbs������ʽ 2��Դ ���ģ�Finding scientific topics
				p[k]=(nkt[k][termIndex]+beta)/(nktSum[k]+V*beta)*
						(nmk[m][k]+alpha)/(nmkSum[m]+K*alpha);
		}
		//ģ������µ�����
		//���ڸ�������ĸ����Ѿ����ɣ����������̸��ʵķ�ʽ,�ж����������ĸ������䣬�������������(0,p[1]),��ôK=1.
		for (int k= 1; k < K; k++) {
			p[k]+=p[k-1];
		}
		double u= Math.random()*p[K-1];
		int newTopic = 0;
		for (int k = 0; k < K; k++) {
			if(u<p[k]){
				newTopic=k;
				break;
			}
		}
		nmk[m][newTopic]++;
		nkt[newTopic][termIndex]++;
		nktSum[newTopic]++;
		nmkSum[m]++;
		
		return newTopic;
	}
	
	/**
	 * ���ã����ݼ�������������ģ�ͱ���
	 * @param isFinalIteration �Ƿ������һ�ε���������Ǿ�Ҫ��ǰ�漸�α���Ľ����ƽ��
	 */
	public void calLatentVariables(boolean isFinalIteration)
	{
//		saveTime++;
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				theta[m][k] += (nmk[m][k]+alpha)/(nmkSum[m]+K*alpha);
				if(isFinalIteration)
					theta[m][k] = theta[m][k] / ((iterations-burn_in) / saveStep + 1); //saveTime;
			}
		}
		for (int k = 0; k < K; k++) {
			for (int v = 0; v < V; v++) {
				phi[k][v] += (nkt[k][v]+beta)/(nktSum[k]+V*beta);
				if(isFinalIteration)
					phi[k][v] = phi[k][v] / ((iterations-burn_in) / saveStep + 1);//saveTime;
			}
		}
		
	}
	
	/**
	 * ���ã����浱ǰ��������ѧϰ����ģ�ͱ���
	 * @param currentIterition�� ��ǰ��������
	 */
	public void saveLatentVariables()
	{
		System.out.println("Save results at iteration ("+iterations+").");
		calLatentVariables(true);
		FilesUtil.saveDistributions(theta, phi);
		FilesUtil.saveTopicAssignment(documents, z);
		FilesUtil.saveTopWords(documents, phi, topNum);

	}
	
}
