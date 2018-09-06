package batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

import dto.POSFileDTO;
import util.BatchUtils;

public class POSItemProcessor implements ItemProcessor<String, JSONObject> {

    private static final Logger log = LoggerFactory.getLogger(POSItemProcessor.class);
	private static final String FILE_SDU = "SDU";
    static int tempVar = 1;
	String fiExc;
	String exceptionMessage = "";
	boolean isFurtherProcessingReqd = false;
	boolean isUserException = false;
	private char batchCount = 'A';

    public JSONObject preprocessSduInputRecord(String obj) throws Exception {
    	POSFileMarshallingBean inputMarshalledRecord = this.marshallPOSFileContents(obj);
    	//Create POSSDURecord from POSFileMarshallingBean
    	POSSDURecord inputRecord = BatchUtils.getSDURecordFromMarshallingBean(inputMarshalledRecord);
    	JSONObject fiRcvCollPostOutput = new JSONObject();
		try {

			boolean isFileEmptyOrInvalid = this.checkFileIfEmptyOrInvalid(inputRecord);
			if (isFileEmptyOrInvalid) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			/*boolean isHeaderValid = this.validateHeaderRecord(inputRecord.getPosHeaderRecord());
			if (!isHeaderValid) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isPosFileUnique = this.checkPosFileUnique(inputRecord.getPosHeaderRecord().get(0).getPostFileSequenceNumber());
			if (!isPosFileUnique) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isAmountNumberic = this.checkAmountDetailRecord(inputRecord.getPosDetailRecord(), FILE_SDU);
			if (!isAmountNumberic) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			//boolean isTrailerValid = this.validateTrailerRecord(inputRecord);
			boolean isTrailerValid = true;
			
			if (!isTrailerValid) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}
*/
			//Map<String, Object> returnMap = this.validateCollBatchDate(inputRecord.getPosDetailRecord(), fiRcvCollPostOutput, FILE_SDU);
			/*List<POSDetailRecord> validatedInputList = (List<POSDetailRecord>) returnMap.get("inputRecords");
			if (null != validatedInputList && !validatedInputList.isEmpty()) {
				inputRecord.setPosDetailRecord(validatedInputList);
			}*/

			//fiRcvCollPostOutput = (POSCollectionRecord) returnMap.get("inputRecords");
			//fiRcvCollPostOutput = this.mapHeaderToOutputObject(inputRecord, fiRcvCollPostOutput);
			//fiRcvCollPostOutput = this.mapTrailerToOutputObject(inputRecord, fiRcvCollPostOutput);
			//fiRcvCollPostOutput.setVtrfOutput(this.createVtrfRecord(inputRecord));
			//fiRcvCollPostOutput = this.mapDetailsToOutputObject(inputRecord, fiRcvCollPostOutput);
			
			fiRcvCollPostOutput = this.mapToOutputObject(inputRecord,fiRcvCollPostOutput);

		} catch (Exception e) {
			log.error("Exception in preprocessSduInputRecord() - FIRcvCollPostBOImpl: " + e.getMessage());
			exceptionMessage = "Exception Occured in preprocessSduInputRecord(): " + e.getMessage();
			isUserException = false;
			isFurtherProcessingReqd = false;
			fiExc = "SDU";
			//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
			return fiRcvCollPostOutput;
		}

		log.info("FIRcvCollPostBOImpl - END of preprocessSduInputRecord(): FiRcvCollPostOutput:" + fiRcvCollPostOutput);

		return fiRcvCollPostOutput;
    }
    
    private POSFileMarshallingBean marshallPOSFileContents(String posFileContents) {
    	String header = posFileContents.substring(1, 21);
    	String detail = posFileContents.substring(25, 127);
    	String trailer = posFileContents.substring(131, 151);
    	
    	POSFileMarshallingBean pos1 = new POSFileMarshallingBean();
    	System.out.println("Header: "+ header);
    	System.out.println("Detail"+ detail);
    	System.out.println("Trailer: "+ trailer);
		 pos1.setPostHeaderRecordType(header.substring(0, 2));
		 pos1.setPostFileSequenceNumber(header.substring(2, 12));
		 pos1.setPostTransmitDate(header.substring(12, 20));
		 
		 pos1.setPostReceiptType(detail.substring(0, 2));
		 pos1.setPostBatchDate(detail.substring(2, 10));
		 pos1.setPostMemberId(detail.substring(10, 20));
		 pos1.setPostScdNumber(detail.substring(20, 34));
		 pos1.setPostShortName(detail.substring(34, 38));
		 pos1.setPostCaseId(detail.substring(38, 47));
		 pos1.setPostDefSsn(detail.substring(47, 56));
		 pos1.setPostColDate(detail.substring(56, 64));
		 pos1.setPostReceiptAmount(detail.substring(64, 73));
		 pos1.setPostPayMethod(detail.substring(73, 74));
		 pos1.setPostSource(detail.substring(74, 77));
		 pos1.setPostEmpId(detail.substring(77, 87));
		 pos1.setPostCheckNum(detail.substring(87, 101));
		 
		 pos1.setPostTrailerRecordType(trailer.substring(0, 2));
		 pos1.setPostRecordCount(trailer.substring(2, 10));
		 pos1.setPostTotalAmount(trailer.substring(10, 19));
		return pos1;
	}

	private JSONObject mapToOutputObject(POSSDURecord inputRecord, JSONObject json) {
    	
		JSONArray fiReceiptArray = new JSONArray();
		JSONObject fiReceipt = new JSONObject();
		JSONArray batchArray = new JSONArray();
		JSONObject batch = new JSONObject();
		POSDetailRecord detail = inputRecord.getPosDetailRecord().get(0);
		
		
		try {
			
			fiReceipt.put("RECEIPT_DT__c", BatchUtils.formatDate(detail.getPostBatchDate()));
			fiReceipt.put("AMOUNT__c", detail.getPostReceiptAmount());
			fiReceipt.put("MEMBER_ID__c", detail.getPostMemberId());
			fiReceipt.put("BATCH_NUMBER__C", inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
			
			fiReceiptArray.put(fiReceipt);
			
			batch.put("TOTAL_BATCH_AMOUNT__C", inputRecord.getPosTrailerRecord().getPostTotalAmount());
			batch.put("BATCH_NUMBER__C", inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
			batch.put("BATCH_DATE__C", BatchUtils.formatDate(inputRecord.getPosHeaderRecord().getPostTransmitDate()));
			batchArray.put(batch);

			json.put("FI_RECIEPTS", fiReceiptArray);
			json.put("BATCH", batchArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return json;
	}

	private boolean checkFileIfEmptyOrInvalid(Object inputRecord) {

		log.info("FIRcvCollPostBOImpl - START of checkFileIfEmptyOrInvalid(): Params: Object:" + inputRecord);
		//SDU
		if (inputRecord instanceof POSSDURecord) {
			POSSDURecord sduInputRecord = (POSSDURecord) inputRecord;

			if ((null == sduInputRecord.getPosHeaderRecord())
					&& (null == sduInputRecord.getPosDetailRecord() || sduInputRecord.getPosDetailRecord().isEmpty())
					&& (null == sduInputRecord.getPosTrailerRecord())) {
				exceptionMessage = "INVALID OR NO DATA IN SDU INPUT FILE. INPUT FILE IS INVALID.";
			} else if (null == sduInputRecord.getPosDetailRecord() || sduInputRecord.getPosDetailRecord().isEmpty()) {
				exceptionMessage = "NO DETAILS RECORD. INPUT FILE IS INVALID.";
			}

			if (!exceptionMessage.isEmpty()) {
				isUserException = true;
				//fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_SDU);
				fiExc = "SDU";
				return true;
			}
		}/* else if (inputRecord instanceof FIRcvCollPostESWRecord) {
			FIRcvCollPostESWRecord eswInputRecord = (FIRcvCollPostESWRecord) inputRecord;

			if (null == eswInputRecord.getCollPostDetailRecords() || eswInputRecord.getCollPostDetailRecords().isEmpty()) {
				exceptionMessage = "INVALID OR NO DATA IN ESW INPUT FILE. INPUT FILE IS INVALID";
			}

			if (!FiConstants.BLANK.equals(exceptionMessage)) {
				isUserException = true;
				fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_ESW);
				return true;
			}
		}  else if (inputRecord instanceof FIRcvCollPostSTRRecord) {
			FIRcvCollPostSTRRecord strInputRecord = (FIRcvCollPostSTRRecord) inputRecord;

			if (null == strInputRecord.getCollPostIFDetailRecords() || strInputRecord.getCollPostIFDetailRecords().isEmpty()) {
				exceptionMessage = "INVALID OR NO DATA IN STROP INPUT FILE. INPUT FILE IS INVALID";
			}

			if (!FiConstants.BLANK.equals(exceptionMessage)) {
				isUserException = true;
				fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_STR);
				return true;
			}
		}  else if (inputRecord instanceof FIRcvCollPostUNCRecord) {
			FIRcvCollPostUNCRecord uncInputRecord = (FIRcvCollPostUNCRecord) inputRecord;

			if (null == uncInputRecord.getCollPostIFDetailRecords() || uncInputRecord.getCollPostIFDetailRecords().isEmpty()) {
				exceptionMessage = "INVALID OR NO DATA IN U/C INPUT FILE. INPUT FILE IS INVALID";
			}

			if (!FiConstants.BLANK.equals(exceptionMessage)) {
				isUserException = true;
				fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_UNC);
				return true;
			}
		}*/ 
		else if (inputRecord instanceof Object) {
			exceptionMessage = "INVALID INPUT FILE.";
			isUserException = true;
			fiExc = "INVALID FILE";
			return true;
		}

		return false;
	}
    
    private Map<String, Object> validateCollBatchDate(List<POSDetailRecord> detailRecordInputList, POSCollectionRecord fiRcvCollPostOutput, String fileType) {

		System.out.println("FIRcvCollPostBOImpl - START of checkCollBatchDateForFuture(): List<FIRcvCollPostDetailRecord> " + detailRecordInputList + "AND FiRcvCollPostOutput: " + fiRcvCollPostOutput);

		Map<String, Object> returnMap = new HashMap<>();
		List<POSDetailRecord> tempRecInputList = new ArrayList<>();

		for (POSDetailRecord tempDetailRec : detailRecordInputList) {
			if (null != tempDetailRec.getPostBatchDate() && containsNonDigit(tempDetailRec.getPostBatchDate().trim())) {
				exceptionMessage = "INVALID COLLECTION BATCH DATE " + tempDetailRec.getPostBatchDate() + "FOR SDU SCD NUMBER " + tempDetailRec.getPostScdNumber();
				isUserException = true;
				isFurtherProcessingReqd = false;
				//fiExc = setExceptionCargo(exceptionMessage, fileType);
				//fiRcvCollPostOutput.getFiExceptionsList().add(fiExc);
				tempRecInputList.add(tempDetailRec);
			}
		}

		if (!tempRecInputList.isEmpty()) {
			detailRecordInputList.removeAll(tempRecInputList);
			returnMap.put("inputRecords", detailRecordInputList);
		}

		returnMap.put("inputRecords", fiRcvCollPostOutput);

		System.out.println("FIRcvCollPostBOImpl - END of checkCollBatchDateForFuture(): List<FIRcvCollPostDetailRecord> " + detailRecordInputList + "AND FiRcvCollPostOutput: " + fiRcvCollPostOutput);

		return returnMap;
	}
    @Override
    public JSONObject process(String obj) throws Exception {

    	JSONObject fiRcvCollPostOutput = null;

		if (null != obj) {
			fiRcvCollPostOutput = this.preprocessSduInputRecord(obj);
		}

		/* Muted for Demo1
		 * if (null != fiRcvCollPostOutput.getvFiBatchReceiptsCargos() && !fiRcvCollPostOutput.getvFiBatchReceiptsCargos().isEmpty()) {
			fiRcvCollPostOutput = fiRcvCollPostBO.validateInputFileCollections(fiRcvCollPostOutput, false, FILE_SDU);
		}*/

		return fiRcvCollPostOutput;
	}
    
    public final boolean containsNonDigit(String inputStr) {
		boolean containsNonDigit = true;

		if (null != inputStr && !inputStr.isEmpty()) {
			for (char c : inputStr.toCharArray()) {
				if ('-' != c) {
					if (containsNonDigit = !Character.isDigit(c)) {
						break;
					}
				}
			}
		}
		return containsNonDigit;
	}

}