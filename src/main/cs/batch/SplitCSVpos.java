package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class SplitCSVpos {
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "postHeaderRecordType, postFileSequenceNumber,"
			+ "postTransmitDate,postReceiptType,postBatchDate,postMemberId,"
			+ "postScdNumber,postShortName,postCaseId,postDefSsn,	"
			+ "postColDate,postReceiptAmount,postPayMethod,postSource,postEmpId,"
			+ "postCheckNum, postTrailerRecordType,postRecordCount,postTotalAmount,";
	public static void writeCsvFile(String fileName) throws FileNotFoundException {
	     Scanner textFile = new Scanner(new File("C:\\Users\\moanuja\\POS1-SDU-20180829-1832.txt"));
		 Set<String> stringSet = new LinkedHashSet<String>();	
		 while (textFile.hasNext()) {
			 stringSet.add(textFile.next().trim());
		 }
		 textFile.close();
		 System.out.println(stringSet);
		 String[] strArr = new String[stringSet.size()];
		 stringSet.toArray(strArr); 
		
		
		 POSFileMarshallingBean pos1 = new POSFileMarshallingBean();
		 pos1.setPostHeaderRecordType(strArr[0].substring(0, 2));
		 pos1.setPostFileSequenceNumber(strArr[0].substring(2, 12));
		 pos1.setPostTransmitDate(strArr[0].substring(12, 20));
		 
		 pos1.setPostReceiptType(strArr[1].substring(0, 2));
		 pos1.setPostBatchDate(strArr[1].substring(2, 10));
		 pos1.setPostMemberId(strArr[1].substring(10, 20));
		 pos1.setPostScdNumber(strArr[1].substring(20, 34));
		 pos1.setPostShortName(strArr[1].substring(34, 38));
		 pos1.setPostCaseId(strArr[1].substring(38, 47));
		 pos1.setPostDefSsn(strArr[1].substring(47, 56));
		 pos1.setPostColDate(strArr[1].substring(56, 64));
		 pos1.setPostReceiptAmount(strArr[1].substring(64, 73));
		 pos1.setPostPayMethod(strArr[1].substring(73, 74));
		 pos1.setPostSource(strArr[1].substring(74, 77));
		 pos1.setPostEmpId(strArr[1].substring(77, 87));
		 pos1.setPostCheckNum(strArr[1].substring(87, 101));
		 
		 pos1.setPostTrailerRecordType(strArr[2].substring(0, 2));
		 pos1.setPostRecordCount(strArr[2].substring(2, 10));
		 pos1.setPostTotalAmount(strArr[2].substring(10, 19));
		 
		 
		 
		 
		List<POSFileMarshallingBean> poslist = new ArrayList<POSFileMarshallingBean>();
		poslist.add(pos1);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName);

			fileWriter.append(FILE_HEADER.toString());
			
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			for (POSFileMarshallingBean pos : poslist) {
				fileWriter.append(pos.getPostHeaderRecordType()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostFileSequenceNumber()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostTransmitDate()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostReceiptType()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostBatchDate()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostMemberId()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostScdNumber()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostShortName()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostCaseId()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostDefSsn()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostColDate()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostReceiptAmount()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostPayMethod()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostSource()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostEmpId()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostCheckNum()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostTrailerRecordType()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostRecordCount()).append(COMMA_DELIMITER);
				fileWriter.append(pos.getPostTotalAmount()).append(COMMA_DELIMITER);
				
			}
			
			System.out.println("CSV file was created successfully !!!");
		}
		catch(Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		}finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		String fileName = System.getProperty("user.home")+"/POS1-SDU-20180829-1832.csv";
		SplitCSVpos.writeCsvFile(fileName);

	}
}
