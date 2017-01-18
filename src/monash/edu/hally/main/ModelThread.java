package monash.edu.hally.main;

import monash.edu.hally.util.Documents;
import monash.edu.hally.util.FilesUtil;


public class ModelThread implements Runnable{
	
	private ModelParameters modelParameters;
	
	public ModelThread(ModelParameters modelParameters)
	{
		this.modelParameters=modelParameters;
	}

	@Override
	public void run() {

		long startTime=System.currentTimeMillis();
		//文档集
		Documents documents=new Documents();
		//扩充字典，并对文档集中的所有文档索引化（将字典的索引和词汇的索引联系上）
		documents.processAllDocuments();	

		//模型处理
		LDAModel ldaModel=new LDAModel(documents,modelParameters);
		//初始化模型参数
		ldaModel.initialiseModel();
		//推断和保存模型的潜在变量
		ldaModel.inferenceModel();
		
		long endTime=System.currentTimeMillis();
		System.out.println("Runtime "+(endTime-startTime)/1000+"s.");
		
		FilesUtil.printSuccessMessage();
	}

}


