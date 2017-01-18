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
		//�ĵ���
		Documents documents=new Documents();
		//�����ֵ䣬�����ĵ����е������ĵ������������ֵ�������ʹʻ��������ϵ�ϣ�
		documents.processAllDocuments();	

		//ģ�ʹ���
		LDAModel ldaModel=new LDAModel(documents,modelParameters);
		//��ʼ��ģ�Ͳ���
		ldaModel.initialiseModel();
		//�ƶϺͱ���ģ�͵�Ǳ�ڱ���
		ldaModel.inferenceModel();
		
		long endTime=System.currentTimeMillis();
		System.out.println("Runtime "+(endTime-startTime)/1000+"s.");
		
		FilesUtil.printSuccessMessage();
	}

}


