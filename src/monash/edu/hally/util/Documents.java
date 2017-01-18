package monash.edu.hally.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import monash.edu.hally.constant.ModelConstants;

public class Documents {
	
	public ArrayList<Document> docs=new ArrayList<Document>();
	
	public ArrayList<String> dictionary=new ArrayList<String>();
	
	public Map<String, Integer> termToIndexMap=new HashMap<String, Integer>();
	
	private ArrayList<File> files=new ArrayList<File>();
	
	/**
	 * 作用：对所有文档索引化
	 */
	public void processAllDocuments()
	{
		if(new File(ModelConstants.DOCUMENTSS_PATH).listFiles().length==0){
			JOptionPane.showMessageDialog(null, "Original documents are null, please add documents.", "Error", JOptionPane.ERROR_MESSAGE);
			System.err.println("Original documents are null, please add documents.");
			System.exit(0);
		}
		Stopwords.readStopwords();	//读取停用词
		findDocuments(ModelConstants.DOCUMENTSS_PATH);
		System.out.println("Begin to extend dictionary and index documents.");
		if(!files.isEmpty())
		{
			int i=0;
			for (File file : files) {
				String documentName=file.getAbsolutePath();
				System.out.println("Reading document["+(++i)+"] "+documentName);
				Document document=new Document(documentName, dictionary, termToIndexMap);
				document.indexProcess();
				docs.add(document);
			}
		}
	}

	/**
	 * 作用：递归找到语料库中的所有文件
	 * @param fileDir: 当前目录路径
	 */
	public void findDocuments(String fileDir)
	{	
		for (File file : new File(fileDir).listFiles())
		{
			if(file.isFile())
				files.add(file);
			else
				findDocuments(file.getAbsolutePath());
		}
	}
	
//	public static void main(String[] args) {
//		Documents documents=new Documents();
//		documents.processAllDocuments();
//		System.out.println(documents.dictionary.size());
//		for (String term : documents.dictionary) {
//			System.out.println(term+"\t"+documents.termToIndexMap.get(term));
//		}
//	}

}
	
