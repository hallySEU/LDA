package monash.edu.hally.main;


import monash.edu.hally.util.Document;
import monash.edu.hally.util.Documents;
import monash.edu.hally.util.FilesUtil;

public class LDAModel extends Thread{

	private double alpha,beta;	//超参(dirichlet分布函数的参数)
	private int M,K,V;	//分别表示文档集中文档的个数，主题个数，字典中的词汇个数
	private double[][] phi;	//主题-词汇分布  维度 K*V
	private double[][] theta;	//文档-主题分布  维度 M*K
	private int[][] nmk;	//每一项为：特定文档下的特定主题出现的次数   维度 M*K
	private int[][] nkt;	//每一项为：特定主题下的特定词汇出现的次数   维度 K*V
	private int[] nktSum;	//每一项为：特定主题下的所有词汇出现的次数和
	private int[] nmkSum;	//每一项为：特定文档下的所有主题出现的次数和
	
	private int[][] z;		//每一项为：特定文档下的特定词汇的主题
	
	private int iterations;	//迭代次数
	private int burn_in;	//burn-in 时期
	private int saveStep;	//burn-in 过后，每saveStep次迭代保存一次结果
	private int topNum;	//显示主题下概率最高的前topNum词项
	
	private Documents documents;	//文档集
	private ModelParameters modelParameters;	//模型参数
//	private int saveTime=0;	//保存次数计数变量
	
	
	
	public LDAModel(Documents documents, ModelParameters modelParameters)
	{
		this.documents=documents;
		this.modelParameters=modelParameters;
		setModelParameters();
	}
	
	/**
	 * 作用：设置需要预先指定的参数
	 */
	public void setModelParameters()
	{
//		System.out.println("Read model parameters.");//另一种读取参数的方式
//		ModelParameters modelParameters=FilesUtil.readParametersFile();
	
		K=modelParameters.getK();
		alpha=50/K;	//一般为 50/K
		beta=0.1;	//一般为 0.1
		iterations=modelParameters.getIterations();
		burn_in=modelParameters.getBurn_in();
		saveStep=modelParameters.getSaveStep();
		topNum=modelParameters.getTopNum();
	}
	
	/**
	 * 作用：初始化模型
	 * 1.初始化模型参数（根据需要学习的文档集得到）
	 * 2.给文档中的词汇随机分配主题
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
			int Nm=document.docWords.length;	//第m篇文档的词数（长度）
			z[m]=new int[Nm];
			for (int n = 0; n < Nm; n++) {
				int zmn=(int) (Math.random()*(K));	//随机分配
				z[m][n]=zmn;
				nmk[m][zmn]++;
				nkt[zmn][document.docWords[n]]++;
				nktSum[zmn]++;
				nmkSum[m]++;
			}
			
		}
	}
	
	/**
	 * 作用：采用Gibbs采样算法，来推断模型参数
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
			{	//不停的采样，直到过了burn-in时期
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
	 * Gibbs采样算法，给当前词汇重新分配新的主题
	 * @return 新的主题
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
			if(modelParameters.getSamplingEquation()==1)//Gibbs采样公式 1来源 论文：Parameter estimation for text analysis	
				p[k]=(nkt[k][termIndex]+beta)/(nktSum[k]+beta)*
						(nmk[m][k]+alpha)/(nmkSum[m]+alpha-1);
			else								//Gibbs采样公式 2来源 论文：Finding scientific topics
				p[k]=(nkt[k][termIndex]+beta)/(nktSum[k]+V*beta)*
						(nmk[m][k]+alpha)/(nmkSum[m]+K*alpha);
		}
		//模拟分配新的主题
		//现在各给主题的概率已经生成，现在用轮盘概率的方式,判断随机数落入的概率区间，例如随机数落入(0,p[1]),那么K=1.
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
	 * 作用：根据计数变量来更新模型变量
	 * @param isFinalIteration 是否是最后一次迭代，如果是就要把前面几次保存的结果求平均
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
	 * 作用：保存当前迭代次数学习到的模型变量
	 * @param currentIterition： 当前迭代次数
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
