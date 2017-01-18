package monash.edu.hally.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import monash.edu.hally.constant.ModelConstants;
import monash.edu.hally.main.ModelParameters;

public class FilesUtil {
	
	static{
		fileDirCheck();
	}
	
	/**
	 * 作用：如果文件目录不存在，就自动创建。
	 */
	public static void fileDirCheck()
	{
		File fileDir=new File(ModelConstants.DOCUMENTSS_PATH);
		if(!fileDir.exists())
			fileDir.mkdirs();
		fileDir=new File(ModelConstants.RESULTS_PATH);
		if(!fileDir.exists())
			fileDir.mkdirs();
		fileDir=new File(ModelConstants.STOPWORDS_PATH);	
		if(!fileDir.exists())
			fileDir.mkdirs();
//		fileDir=new File(ModelConstants.PARAMETERS_PATH);
//		if(!fileDir.exists())
//			fileDir.mkdirs();
	}
	
	/**
	 * 作用：将一篇文档以一行数据为单位存入ArrayList中，目的为了分词。
	 * @param documentName:源文件的绝对路径名
	 */
	public static ArrayList<String> readDocument(String documentName)
	{
		try {
			BufferedReader reader=new BufferedReader(new FileReader(new File(documentName)));
			String line;
			ArrayList<String> documentLines=new ArrayList<String>();
			while((line=reader.readLine())!=null)
			{
				documentLines.add(line.trim());
			}
			return documentLines;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 作用：保存当前迭代次数的 文档-主题分布 和 主题-词汇分布
	 * @param currentIterition 迭代次数
	 * @param theta 文档-主题分布
	 * @param phi 主题-词汇分布
	 */
	public static void saveDistributions(double[][]theta,double[][]phi)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".theta")));
			for (int m = 0; m < theta.length; m++) {
				for (int k = 0; k < theta[0].length; k++) {
					writer.write(theta[m][k]+"\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
			writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".phi")));
			for (int k = 0; k < phi.length; k++) {
				for (int v = 0; v < phi[0].length; v++) {
					writer.write(phi[k][v]+"\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 作用：保存文档中词汇所分配的主题
	 * @param currentIterition 当前迭代次数
	 * @param documents	文档集
	 * @param z 文档集中所有词汇的主题分配
	 */
	public static void saveTopicAssignment(Documents documents,int[][]z)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".topic_assignment")));
			for (int m = 0; m < z.length; m++) {
				Document document=documents.docs.get(m);
				for (int n = 0; n < document.docWords.length; n++) {
					int termIndex=document.docWords[n];
					writer.write(documents.dictionary.get(termIndex)+"["+termIndex+"]:"+z[m][n]);
					writer.write("\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 作用：保存所有主题下，概率最高的TOP_NUM（默认为10）个词项
	 * @param currentIterition 当前迭代次数
	 * @param documents 文档集
	 * @param phi 主题-词汇分布
	 */
	public static void saveTopWords(Documents documents,double[][]phi,int topNum)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".topwords")));
			for (int k = 0; k < phi.length; k++) {
				ArrayList<Integer> arrayList=new ArrayList<Integer>();
				for (int v = 0; v < phi[0].length; v++) {
					arrayList.add(new Integer(v));				
				}
				Collections.sort(arrayList,new TopWordComparable(phi[k]));
				writer.write("topic "+(k+1)+" : ");
				for (int i = 0; i < topNum; i++) {
					writer.write(documents.dictionary.get(arrayList.get(i))+"\t");
					writer.write(phi[k][arrayList.get(i)]+"\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 作用：处理汉语分词.(英文默认不处理)
	 * @param line
	 */
	public static String chineseTokenize(String line)
	{
		 StringReader sr=new StringReader(line);  	 
         IKSegmenter ik=new IKSegmenter(sr, true);  
         Lexeme lex=null;
         String newLine="";
         try {
			while((lex=ik.next())!=null){  
				newLine += lex.getLexemeText()+" ";  
			 }
			return newLine;
		} catch (IOException e) {
			e.printStackTrace();
		}  
         return null;
	}
	/**
	 * 作用：分词
	 * @param documentLines
	 */
	public static ArrayList<String> tokenize(ArrayList<String> documentLines)
	{
		ArrayList<String> tokens=new ArrayList<String>();
		for (String line : documentLines) {
			StringTokenizer tokenizer=new StringTokenizer(chineseTokenize(line));
			while(tokenizer.hasMoreTokens())
			{
				String token=tokenModify(tokenizer.nextToken());	//去掉词汇最后部分的标点符号	
				if(!isNoiseWord(token)&&!Stopwords.isContains(token))//去噪和去停用词
					tokens.add(token.toLowerCase().trim());
			}	
		}
		return tokens;
	}
	
	/**
	 * 作用：去掉词汇最后部分的标点符号	
	 */
	public static String tokenModify(String token)
	{
		String subToken=token.substring(0, token.length()-1);
		return subToken+token.substring(token.length()-1).replaceAll("\\pP|\\pS", "");
	}

	/**
	 * 作用：去噪
	 * @param token
	 */
	public static boolean isNoiseWord(String token) {
		token = token.toLowerCase().trim();
		// filter @xxx and URL
		if(token.matches(".*www\\..*") || token.matches(".*\\.com.*") || 
				token.matches(".*http:.*") )
			return true;
		return false;
	}

	
	/**
	 * 作用：打印成功的信息
	 */
	public static void printSuccessMessage()
	{
		String resultPath=System.getProperty("user.dir")+"\\data\\results";
//		JOptionPane.showMessageDialog(null, "Results are reserved in "+resultPath);
		System.out.println("Results are reserved in "+resultPath);
		try {	
			int choice=JOptionPane.showConfirmDialog(null, "Results are reserved in "+resultPath+
					"\nDo you want to open the dir of results ?", "Make a choice", JOptionPane.YES_NO_OPTION);
			if(choice==JOptionPane.OK_OPTION)
				java.awt.Desktop.getDesktop().open(new File(resultPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 作用：创建默认的参数文件 （从文件中读参数，用于非图形化界面）
	 */
	public static void createParametersFile()
	{
		File file=new File(ModelConstants.PARAMETERS_PATH+ModelConstants.PARAMETERS_FILE);
		try {
			file.createNewFile();
			PrintWriter writer=new PrintWriter(file);
			
			writer.print("K (Number of topics):"+ModelConstants.SPLIT_FLAG+"5"+ModelConstants.CR_LF);
			writer.print("Top number:"+ModelConstants.SPLIT_FLAG+"10"+ModelConstants.CR_LF);
			writer.print("Iterations:"+ModelConstants.SPLIT_FLAG+"100"+ModelConstants.CR_LF);
			writer.print("Burn_in:"+ModelConstants.SPLIT_FLAG+"80"+ModelConstants.CR_LF);
			writer.print("SaveStep:"+ModelConstants.SPLIT_FLAG+"10"+ModelConstants.CR_LF);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * 作用：从参数文件中读取模型参数（从文件中读参数，用于非图形化界面）
	 */
	public static ModelParameters readParametersFile()
	{
		File file=new File(ModelConstants.PARAMETERS_PATH+ModelConstants.PARAMETERS_FILE);
		ModelParameters modelParameters=new ModelParameters();
		if(!file.exists())
			createParametersFile();
		
		ArrayList<String> lines = readDocument(file.getAbsolutePath());
		for (String line : lines) {
			String[] para=line.split(ModelConstants.SPLIT_FLAG);
			switch (para[0]) {
			case "Iterations:":
				modelParameters.setIterations(Integer.valueOf(para[1]));
				break;
			case "Burn_in:":
				modelParameters.setBurn_in(Integer.valueOf(para[1]));
				break;
			case "SaveStep:":
				modelParameters.setSaveStep(Integer.valueOf(para[1]));
				break;
			case "Top number:":
				modelParameters.setTopNum(Integer.valueOf(para[1]));
				break;
			default:
				modelParameters.setK(Integer.valueOf(para[1]));
				break;
			}
		}
		return modelParameters;
	}
	
}
