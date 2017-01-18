package monash.edu.hally.util;

import java.util.ArrayList;
import java.util.Map;

public class Document
{
	public int docWords[];
	private String documentName;
	private ArrayList<String> dictionary;
	private Map<String, Integer> termToIndexMap;
	
	public Document(String documentName,ArrayList<String> dictionary,Map<String, Integer> termToIndexMap)
	{
		this.documentName=documentName;
		this.dictionary=dictionary;
		this.termToIndexMap=termToIndexMap;
	}
	
	/**
	 * ���ã����ĵ��еĴʻ���ӵ��ֵ��С�
	 * �ĵ��е�ÿһ���ʻ�����n����docWords[n]����ʾ���ֵ��ж�Ӧ�Ĵ�������
	 */
	public void indexProcess()
	{
		ArrayList<String> documentLines=FilesUtil.readDocument(documentName);
		ArrayList<String> tokens = FilesUtil.tokenize(documentLines);
		docWords=new int[tokens.size()];
		for (int n = 0; n < tokens.size(); n++) {
			String token=tokens.get(n);
			if(!termToIndexMap.keySet().contains(token))// dictionary.contains(token))
			{
				int dictionarySize=dictionary.size();
				termToIndexMap.put(token, dictionarySize);
				docWords[n]=dictionarySize;
				dictionary.add(token);
			}
			else
				docWords[n]=termToIndexMap.get(token);
		}
	}
}
	
