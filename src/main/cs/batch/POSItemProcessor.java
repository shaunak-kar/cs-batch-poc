	package batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.ResponseEntity;

import constants.FiConstants;
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

	//TODO: Integrate All Validations
	public JSONObject preprocessSduInputRecord(String obj) throws Exception {
		POSFileMarshallingBean inputMarshalledRecord = this.marshallPOSFileContents(obj);
		//Create POSSDURecord from POSFileMarshallingBean
		POSSDURecord inputRecord = BatchUtils.getSDURecordFromMarshallingBean(inputMarshalledRecord);
		JSONObject fiRcvCollPostOutput = new JSONObject();
		try {

			boolean isFileEmptyOrInvalid = this.checkFileIfEmptyOrInvalid(inputRecord);
			if (isFileEmptyOrInvalid) {
				log.info("File is not valid");
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isHeaderValid = this.validateHeaderRecord(inputRecord.getPosHeaderRecord());

			if (!isHeaderValid) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isPosFileUnique = this.checkPosFileUnique(inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
			if (!isPosFileUnique) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isAmountNumberic = this.checkAmountDetailRecord(inputRecord.getPosDetailRecord(), FILE_SDU);
			if (!isAmountNumberic) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			boolean isTrailerValid = this.validateTrailerRecord(inputRecord);
			//boolean isTrailerValid = true;

			if (!isTrailerValid) {
				//fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
				return fiRcvCollPostOutput;
			}

			Map<String, Object> returnMap = this.validateCollBatchDate(inputRecord.getPosDetailRecord(), fiRcvCollPostOutput, FILE_SDU);
			List<POSDetailRecord> validatedInputList =  (List<POSDetailRecord>) returnMap.get(FiConstants.MAP_KEY_INPUT);
			if (null != validatedInputList && !validatedInputList.isEmpty()) {
				inputRecord.setPosDetailRecord(validatedInputList);
			}


			//fiRcvCollPostOutput = (POSCollectionRecord) returnMap.get("inputRecords");
			//fiRcvCollPostOutput = this.mapHeaderToOutputObject(inputRecord, fiRcvCollPostOutput);
			//fiRcvCollPostOutput = this.mapTrailerToOutputObject(inputRecord, fiRcvCollPostOutput);
			//fiRcvCollPostOutput.setVtrfOutput(this.createVtrfRecord(inputRecord));
			//fiRcvCollPostOutput = this.mapDetailsToOutputObject(inputRecord, fiRcvCollPostOutput);


			if (null != inputRecord) {
				inputRecord = validateInputFileCollections(inputRecord, false, FILE_SDU);
			}
			log.error("Validate Inputfile has been done.." + inputRecord);
			
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



		String header = posFileContents.substring(0, 21);
		System.out.println("Header: "+ header);
		String detail = posFileContents.substring(22, 125);
		System.out.println("Detail"+ detail);
		String trailer = posFileContents.substring(126, 146);
		System.out.println("Trailer: "+ trailer);

		POSFileMarshallingBean pos1 = new POSFileMarshallingBean();
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
		pos1.setPostTotalAmount(trailer.substring(10, 20));
		return pos1;
	}

	private JSONObject mapToOutputObject(POSSDURecord inputRecord, JSONObject json) {

		JSONArray fiReceiptArray = new JSONArray();
		JSONObject fiReceipt = new JSONObject();
		JSONArray batchArray = new JSONArray();
		JSONObject batch = new JSONObject();
		JSONArray UnpostedReceiptArray = new JSONArray();
		JSONObject UnpostedReceipt = new JSONObject();
		
		POSDetailRecord detail = inputRecord.getPosDetailRecord().get(0);
		String ReciptNumber = detail.getCountyCode()+inputRecord.getPosHeaderRecord().getPostTransmitDate() +  detail.getPostPayMethod() + "A0001" + "001" +"000";
		String batchId = ReciptNumber.substring(0,15); 
		try {

			if(!detail.getIsUnpostedRecord()) {
			fiReceipt.put("RECEIPT_DT__c", BatchUtils.formatDate(detail.getPostBatchDate()));
			fiReceipt.put("AMOUNT__c", detail.getPostReceiptAmount());
			fiReceipt.put("MEMBER_ID__c", detail.getPostMemberId());
			fiReceipt.put("MEMBER_SSN__c", detail.getPostDefSsn());
			fiReceipt.put("MEMBER_SHORT__c", detail.getPostShortName());
			fiReceipt.put("BATCH_NUMBER__C", inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
			fiReceipt.put("RECEIPT_NUM__c", ReciptNumber);
			fiReceipt.put("RECEIPT_STATUS__c", detail.getReceiptStatus());
			fiReceipt.put("REASON_CD__c", detail.getReasonCd());

			fiReceiptArray.put(fiReceipt);
			json.put("FI_RECIEPTS", fiReceiptArray);
			
			json.put("UNPOSTED_RECEIPT", UnpostedReceiptArray);
			}else 
			{
				
				
				UnpostedReceipt.put("Collection_Date__c", BatchUtils.formatDate(detail.getPostBatchDate()));
				UnpostedReceipt.put("Amount__c", detail.getPostReceiptAmount());
				UnpostedReceipt.put("Case_Number__c", detail.getPostCaseId());
				UnpostedReceipt.put("External_Member_Id__c", detail.getPostMemberId());
				UnpostedReceipt.put("Member_SSN__c", detail.getPostDefSsn());
				UnpostedReceipt.put("Member_Last_Name__c", detail.getPostShortName());
				UnpostedReceipt.put("Payment_Method__c", detail.getPostPayMethod());
				UnpostedReceipt.put("Payment_Source__c", detail.getPostSource());
				UnpostedReceipt.put("Scu_Scd_Number__c", detail.getPostScdNumber());
				UnpostedReceipt.put("Batch_Id__c", batchId);
				UnpostedReceipt.put("Batch_Number__c", inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
				
				UnpostedReceiptArray.put(UnpostedReceipt);
				json.put("UNPOSTED_RECEIPT", UnpostedReceiptArray);
				
				json.put("FI_RECIEPTS", fiReceiptArray);
			}
			

			batch.put("TOTAL_BATCH_AMOUNT__C", inputRecord.getPosTrailerRecord().getPostTotalAmount());
			batch.put("BATCH_NUMBER__C", inputRecord.getPosHeaderRecord().getPostFileSequenceNumber());
			batch.put("BATCH_DATE__C", BatchUtils.formatDate(inputRecord.getPosHeaderRecord().getPostTransmitDate()));
			batch.put("PAYMENT_SOURCE__c", detail.getPostSource());
			batch.put("PAYMENT_METHOD__c", detail.getPostPayMethod());
			batch.put("BATCH_ID__c",batchId );

			batchArray.put(batch);

			
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
	private boolean validateHeaderRecord(Object inputRecord) {

		log.info("FIRcvCollPostBOImpl - START of validateHeaderRecord(): Params: FIRcvCollPostHeaderRecord: "  + inputRecord);


		if (null == inputRecord ) {
			exceptionMessage = "NO HEADER RECORD. INPUT FILE IS INVALID";
		} /*else if (headerRecords.size() > FiConstants.INTEGER_ONE) {
			exceptionMessage = "MULTIPLE HEADER RECORDS. INPUT FILE IS INVALID.";
		} */else {
			POSHeaderRecord header = (POSHeaderRecord) inputRecord;
			log.info( getRecordType(header.getPostHeaderRecordType()) + header.getPostFileSequenceNumber());



			if (null != header.getPostHeaderRecordType() && FiConstants.RECORD_TYPE_H.equals( getRecordType(header.getPostHeaderRecordType()))) {
				if (null == header.getPostFileSequenceNumber() || header.getPostFileSequenceNumber().isEmpty() || Long.valueOf(header.getPostFileSequenceNumber()) == 0) {
					exceptionMessage = "WRONG HEADER SIZE. INPUT FILE IS INVALID.";
				}
			}
		}

		if (!FiConstants.BLANK.equals(exceptionMessage)) {
			isUserException = true;
			//fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_SDU);
			return false;
		}
		return true;
	}

	/**
	 * @param posSeqNum
	 * @return
	 * @throws GenericRunTimeException
	 */
	private boolean  checkPosFileUnique(String posSeqNum) {

		log.info("FIRcvCollPostBOImpl - START of checkPosFileUnique(): Params: POS Seq Num: " + posSeqNum);

		ResponseEntity<String> resp = null;

		JSONObject memJson = new JSONObject();
		try {


			memJson.put("fileSequenceNumber",posSeqNum);
			resp  = BatchUtils.getDataFromRestCall("https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/apexrest/validateheader/",memJson.toString() );

		} catch (Exception e) {
			System.out.println("--EXCEPTION---"+e.getMessage() );

			e.printStackTrace();



		}
		Boolean isPOsSeqNumUnique = Boolean.parseBoolean(resp.getBody());
		System.out.println("Is file Unique : "+ isPOsSeqNumUnique);

		return isPOsSeqNumUnique;
	}


	/**
	 * @param collPostDetailRecords
	 * @param fileType
	 * @return
	 */
	private boolean checkAmountDetailRecord(List<POSDetailRecord> collPostDetailRecords, String fileType) {

		log.info("FIRcvCollPostBOImpl - START of checkAmountDetailRecord(): Params: List<FIRcvCollPostDetailRecord>: " + (POSDetailRecord)collPostDetailRecords.get(0));

		for (POSDetailRecord detailRecord : collPostDetailRecords) {
			String amount = detailRecord.getPostReceiptAmount().trim();
			if (containsNonDigit(amount)) {
				exceptionMessage = "AMOUNT MUST BE NUMERIC.";
				isUserException = true;
				isFurtherProcessingReqd = false;
				//fiExc = setExceptionCargo(exceptionMessage, fileType);
				return false;
			}
		}
		return true;
	}

	/**
	 * @param inputRecord
	 * @return
	 */
	private boolean validateTrailerRecord(POSSDURecord inputRecord) {

		log.info("TrailerRecord - START of validateTrailerRecord(): Params: FIRcvCollPostDetailRecord: " + inputRecord + inputRecord.getPosTrailerRecord() + inputRecord.getPosTrailerRecord().getPostRecordCount());

		POSTrailerRecords trailer =  inputRecord.getPosTrailerRecord();

		if (null == trailer ) {
			exceptionMessage = "NO TRAILER RECORD. INPUT FILE IS INVALID.";
		}  else {

			if (null == trailer.getPostRecordCount() || null == trailer.getPostTotalAmount()
					|| trailer.getPostRecordCount().isEmpty() || trailer.getPostTotalAmount().isEmpty()
					|| containsNonDigit(trailer.getPostRecordCount()) || containsNonDigit(trailer.getPostTotalAmount())) {
				exceptionMessage = "WRONG TRAILER SIZE. INPUT FILE IS INVALID.";
			}
		}

		if (!FiConstants.BLANK.equals(exceptionMessage)) {
			log.info(exceptionMessage);
			isUserException = true;
			isFurtherProcessingReqd = false;
			//fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_SDU);
			return false;
		}

		int totalDetailRecCount = inputRecord.getPosDetailRecord().size();
		double totalDetailRecAmount = FiConstants.DOUBLE_ZERO;

		for (POSDetailRecord tempDetailRec : inputRecord.getPosDetailRecord()) {
			double tempAmount = Double.parseDouble(checkForNegativeAmount(tempDetailRec.getPostReceiptAmount()));
			if (null != tempDetailRec && tempAmount > 0) {
				totalDetailRecAmount += tempAmount;
			}
		}

		if (totalDetailRecCount != Integer.parseInt(trailer.getPostRecordCount())) {
			exceptionMessage = "OUT-OF-BAL-RECORD COUNT DOES NOT MATCH.";
		} else if (totalDetailRecAmount != Double.parseDouble(checkForNegativeAmount(trailer.getPostTotalAmount()))) {
			exceptionMessage = "OUT-OF-BAL-AMOUNT DOES NOT MATCH.";
		}

		if (!FiConstants.BLANK.equals(exceptionMessage)) {
			isUserException = true;
			isFurtherProcessingReqd = false;
			//fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_SDU);
			return false;
		}

		return true;
	}
	/**
	 * @param input
	 * @return
	 */
	private String checkForNegativeAmount(String input) {
		String output = FiConstants.BLANK;

		if (null != input && !FiConstants.BLANK.equals(input.trim())) {
			output = input.replaceFirst (FiConstants.REGEX_LEAD_ZERO, FiConstants.BLANK);
		}

		return (null == output || FiConstants.BLANK.equals(output) ? FiConstants.STRING_ZERO : output) ;
	}

	/**
	 * @param detailRecordInputList
	 * @param fiRcvCollPostOutput
	 * @param fileType
	 * @return
	 */
	private Map<String, Object> validateCollBatchDate(List<POSDetailRecord> detailRecordInputList, JSONObject fiRcvCollPostOutput, String fileType) {

		log.info("FIRcvCollPostBOImpl - START of checkCollBatchDateForFuture(): List<FIRcvCollPostDetailRecord> " + detailRecordInputList + "AND FiRcvCollPostOutput: " + fiRcvCollPostOutput);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<POSDetailRecord> tempRecInputList = new ArrayList<POSDetailRecord>();


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
			returnMap.put(FiConstants.MAP_KEY_INPUT, detailRecordInputList);
		}

		returnMap.put(FiConstants.MAP_KEY_OUTPUT, fiRcvCollPostOutput);

		log.info("FIRcvCollPostBOImpl - END of checkCollBatchDateForFuture(): List<FIRcvCollPostDetailRecord> " + detailRecordInputList + "AND FiRcvCollPostOutput: " + fiRcvCollPostOutput);

		return returnMap;
	}


	/**
	 * @param recordCode
	 * @return
	 */
	private String getRecordType(String recordCode) {
		String type = "";

		switch(recordCode) {
		case FiConstants.CASE_01:
			type = FiConstants.RECORD_TYPE_H;
			break;
		case FiConstants.CASE_02:
			type = FiConstants.RECORD_TYPE_D;
			break;
		case FiConstants.CASE_03:
			type = FiConstants.RECORD_TYPE_T;
			break;
		default :
			type = FiConstants.BLANK;
			break;
		}
		return type;
	}


	/**
	 * This method performs validation functions on the pre-processed input file received by SDU or other sub-systems.
	 * @throws ParseException 
	 */
	public POSSDURecord validateInputFileCollections(POSSDURecord inputRecord, boolean isFileIrs, String fileType) throws ParseException {

		log.info("POSItemProcessor - START of validateInputFileCollections(): FiRcvCollPostOutput: " + inputRecord );
		List<POSDetailRecord> detailRecordList = inputRecord.getPosDetailRecord();
		POSDetailRecord detailRecord = detailRecordList.get(0);
		detailRecord.setIsUnpostedRecord(false);
		detailRecord.setCountyCode("21");
		
		
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Date collectionDate = simpleDateFormat.parse(BatchUtils.formatDate(detailRecord.getPostBatchDate()));
		Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date() ));
		log.info("Collection date --"  +collectionDate.toString() +" ----- "+today.toString()+"-----"+collectionDate.after(today));
	
		try {
			log.info("FIRcvCollPostBOImpl - START of validateInputFileCollections(): FiRcvCollPostOutput: " + inputRecord);
			
			//Refer the Collection Document -- Section --	Validation and Identification of Type of Receipts


			if (Double.valueOf(detailRecord.getPostReceiptAmount()) <= 0) {
				detailRecord.setIsUnpostedRecord(true);
						/*fiUnposList.add(fiUnpostedReciptsCargo);
						isFurtherProcessingReqd = true;
						isUserException = true;
						fiExc = setExceptionCargo("Receipt with SDU Number "+ fiUnpostedReciptsCargo.getSduScdNumber()+" and Employee ID "
								+fiUnpostedReciptsCargo.getEmpId()+" has amount less than or equalling 0.", FiConstants.FILE_SDU);
						if (null != fiExc && null != fiExc.getExceptionSummary() && !FiConstants.BLANK.equals(fiExc.getExceptionSummary())) {
							fiExceptionsList.add(fiExc);
						}*/
				detailRecordList.set(0,  detailRecord);
				inputRecord.setPosDetailRecord(detailRecordList);
				return inputRecord;
				
			}
			
			
			if (collectionDate.after(today)) {
						/*isFurtherProcessingReqd = true;
						isUserException = true;
						fiExc = setExceptionCargo("Invalid Collection Batch Date " + receiptsCargo.getBatchDate() + " for SDU Number " + receiptsCargo.getSduScdNumber() +". Collection Batch Date cannot be a future date.", FiConstants.FILE_SDU);
						if (null != fiExc && null != fiExc.getExceptionSummary() && !FiConstants.BLANK.equals(fiExc.getExceptionSummary())) {
							fiExceptionsList.add(fiExc);
						}
						continue;*/
				detailRecord.setIsUnpostedRecord(true);
				detailRecordList.set(0,  detailRecord);
				inputRecord.setPosDetailRecord(detailRecordList);
				return inputRecord;
			}

			JSONObject jsonBody = new JSONObject();
			JSONObject detailJSON = new JSONObject();
			detailJSON.put("memberId",detailRecord.getPostMemberId());
			detailJSON.put("memberSSN",detailRecord.getPostDefSsn());
			detailJSON.put("memberLast",detailRecord.getPostShortName());

			jsonBody.put("jsoninput", detailJSON);
			System.out.println(jsonBody.toString());
			log.info(jsonBody.toString());
			ResponseEntity<String>	 resp  = BatchUtils.getDataFromRestCall("https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/apexrest/validateMemberId/",jsonBody.toString() );

			System.out.println(resp.getBody());
			
			JSONObject respBody = new JSONObject(resp.getBody());
			

			boolean isNameAndSsnMatch = false;
			boolean isMemberIdExist =  Boolean.parseBoolean( respBody.getString("isMemberIdExisting"));
			boolean isNameAndLastNameMatch =  Boolean.parseBoolean( respBody.getString("isMemberLastNameMatched"));
			boolean isSSNExist =  Boolean.parseBoolean( respBody.getString("isSSNExisting"));
			String memberIdBySSN  = "";
			if(respBody.has("memberId")) {
			
			 memberIdBySSN =  respBody.getString("memberId");
			}
			
			boolean isCaseExist = true;
			boolean isCaseValid = true;
			
			


			if (null != detailRecord.getPostMemberId() && !FiConstants.BLANK.equalsIgnoreCase(detailRecord.getPostMemberId().trim())) {
						
						
						if(isMemberIdExist) {
							if(isNameAndLastNameMatch) 
							{
								detailRecord.setReceiptStatus(FiConstants.IDENTIFIED_RECEIPT);
							}else 
							{
								detailRecord = setValuesInReceiptsCargo(detailRecord, FiConstants.UNIDENTIFIED_RECEIPT, FiConstants.UNIDENTIFIED_COUNTY,FiConstants.REASON_CODE_CMU);

							}
						}else {
							
							detailRecord = setValuesInReceiptsCargo(detailRecord, FiConstants.UNIDENTIFIED_RECEIPT, FiConstants.UNIDENTIFIED_COUNTY,FiConstants.REASON_CODE_CMU);

						}
						
			} else if (null != detailRecord.getPostDefSsn() && !FiConstants.BLANK.equalsIgnoreCase(detailRecord.getPostDefSsn().trim())){

				if(isSSNExist) {
					if(isNameAndSsnMatch && !memberIdBySSN.isEmpty()) 
					{
						detailRecord.setPostMemberId(memberIdBySSN);
						detailRecord.setReceiptStatus(FiConstants.IDENTIFIED_RECEIPT);
					}else
					{
						detailRecord = setValuesInReceiptsCargo(detailRecord, FiConstants.UNIDENTIFIED_RECEIPT, FiConstants.UNIDENTIFIED_COUNTY,FiConstants.REASON_CODE_CMU);

					}
				}else {
					detailRecord = setValuesInReceiptsCargo(detailRecord, FiConstants.UNIDENTIFIED_RECEIPT, FiConstants.UNIDENTIFIED_COUNTY,FiConstants.REASON_CODE_CMU);

				}
			}
			
			
			//Check if case is present. if present is it valid ?	
			if(!isCaseExist || (isCaseExist && !isCaseValid )) 
			{
				detailRecord.setPostCaseId(FiConstants.STRING_ZERO);
				
			}
				
				// get and set county if county code is empty
				if(null != detailRecord.getReceiptStatus() && FiConstants.IDENTIFIED_RECEIPT.equals(detailRecord.getReceiptStatus())
						&& (null == detailRecord.getCountyCode() || FiConstants.BLANK.equals(detailRecord.getCountyCode().trim())))
						{
					
						}
				
				if (null == detailRecord.getCountyCode() || FiConstants.BLANK.equals(detailRecord.getCountyCode())) {
					detailRecord.setCountyCode(FiConstants.UNIDENTIFIED_COUNTY);
				}
				

				
				boolean isValidPayMethod = (null != detailRecord.getPostPayMethod() && !FiConstants.BLANK.equals(detailRecord.getPostPayMethod()) && !FiConstants.INVALID_PAYMENT_METHOD.equalsIgnoreCase(detailRecord.getPostPayMethod()))
							? true : false;
							
				boolean isValidPaySource = (null != detailRecord.getPostSource() && !FiConstants.BLANK.equals(detailRecord.getPostSource()) && !FiConstants.INVALID_PAYMENT_SOURCE.equalsIgnoreCase(detailRecord.getPostSource()))
									? true : false;

									if (!isValidPayMethod || (FiConstants.FILE_STR.equals(fileType) && !FiConstants.PAYMENT_METHOD_S.equals(detailRecord.getPostPayMethod()))
											|| (FiConstants.FILE_UNC.equals(fileType) && !FiConstants.PAYMENT_METHOD_U.equals(detailRecord.getPostPayMethod()))) {
										detailRecord.setPostPayMethod(FiConstants.INVALID_PAYMENT_METHOD);
										if (FiConstants.IDENTIFIED_RECEIPT.equalsIgnoreCase(detailRecord.getReceiptStatus())) {
											detailRecord.setReceiptStatus(FiConstants.HELD_RECEIPT);
											detailRecord.setReasonCd(FiConstants.REASON_CODE_CPM);
										}
									} else {
										detailRecord.setPostPayMethod(detailRecord.getPostPayMethod());
									}

									if (!isValidPaySource || (FiConstants.FILE_STR.equals(fileType) && !FiConstants.PAYMENT_SOURCE_STR.equals(detailRecord.getPostSource()))
											|| (FiConstants.FILE_UNC.equals(fileType) && !FiConstants.PAYMENT_SOURCE_UNC.equals(detailRecord.getPostSource()))) {
										detailRecord.setPostSource(FiConstants.INVALID_PAYMENT_SOURCE);
										if (FiConstants.IDENTIFIED_RECEIPT.equalsIgnoreCase(detailRecord.getReceiptStatus())) {
											detailRecord.setReceiptStatus(FiConstants.HELD_RECEIPT);
											if (null != detailRecord.getReasonCd()&& FiConstants.REASON_CODE_CPM.equalsIgnoreCase(detailRecord.getReasonCd())) {
												detailRecord.setReasonCd(FiConstants.REASON_CODE_CMS);
											} else {
												detailRecord.setReasonCd(FiConstants.REASON_CODE_CPS);
											}
										}
									} else {
										detailRecord.setPostSource(detailRecord.getPostSource());
									}
									
									
									
									
									

									if (null != detailRecord.getReceiptStatus() && null != detailRecord.getPostPayMethod()) {
										if (FiConstants.PAYMENT_METHOD_S.equalsIgnoreCase(detailRecord.getPostPayMethod())
												|| FiConstants.PAYMENT_METHOD_I.equalsIgnoreCase(detailRecord.getPostPayMethod())
												|| FiConstants.PAYMENT_METHOD_U.equalsIgnoreCase(detailRecord.getPostPayMethod()) || isFileIrs) {
											detailRecord.setCountyCode(FiConstants.IRS_COUNTY);
										}
										
										// we are not validating for Payment Method -- Check(C),Escrow(E).
										
									/*	if (FiConstants.PAYMENT_METHOD_K.equalsIgnoreCase(detailRecord.getPostPayMethod())) {
											CiAppIndvCargo[] ciAppCargo = new CiAppIndvCargo[receiptList.size()];
											if (null != vCiCaseIndvDetailsCargos && vCiCaseIndvDetailsCargos.length > FiConstants.INTEGER_ZERO) {
												ciAppCargo = ciMemberBo.getMemberDetails(vCiCaseIndvDetailsCargos[0].getIndvId());
											}

											if (null != ciAppCargo && ciAppCargo.length > FiConstants.INTEGER_ZERO && null != ciAppCargo[0]) {
												if (FiConstants.CHAR_NULL != ciAppCargo[0].getNsfInd() && FiConstants.CHAR_Y == Character.toUpperCase(ciAppCargo[0].getNsfInd())) {
													if (FiConstants.IDENTIFIED_RECEIPT.equalsIgnoreCase(receiptsCargo.getReceiptStatus()) ) {
														receiptsCargo = setValuesInReceiptsCargo(receiptsCargo, FiConstants.HELD_RECEIPT, null, FiConstants.REASON_CODE_CNF);
													}
													if (FiConstants.UNIDENTIFIED_RECEIPT.equalsIgnoreCase(receiptsCargo.getReceiptStatus()) ) {
														receiptsCargo.setReasonCd(FiConstants.REASON_CODE_CNF);
													}
												}
											}
										}

										if (FiConstants.PAYMENT_METHOD_E.equalsIgnoreCase(receiptsCargo.getPaymentMethod())) {
											FiEscrowInfoCargo fiEscInfoCargo = new FiEscrowInfoCargo();
											FiEscrowInfoCollection fiEscColl = new FiEscrowInfoCollection();
											FiEscrowBondInfoBO fiEscBo = new FiEscrowBondInfoBO();
											if (null != vCiCaseIndvDetailsCargos && vCiCaseIndvDetailsCargos.length > FiConstants.INTEGER_ZERO) {
												fiEscInfoCargo.setPayorMemberId(String.valueOf(vCiCaseIndvDetailsCargos[0].getIndvId()));
											}
											fiEscColl = fiEscBo.getEscrowDetails(fiEscInfoCargo);
											if (null == fiEscColl || fiEscColl.isEmpty()) {
												if (FiConstants.IDENTIFIED_RECEIPT.equalsIgnoreCase(receiptsCargo.getReceiptStatus())) {
													receiptsCargo = setValuesInReceiptsCargo(receiptsCargo, FiConstants.HELD_RECEIPT, null, FiConstants.REASON_CODE_CES);
												}
												if (FiConstants.UNIDENTIFIED_RECEIPT.equalsIgnoreCase(receiptsCargo.getReceiptStatus())) {
													receiptsCargo.setReasonCd(FiConstants.REASON_CODE_CES);
												}
											}
										}*/
									}
									

									detailRecordList.set(0,  detailRecord);
									inputRecord.setPosDetailRecord(detailRecordList);
									
		} catch (Exception e) {
			log.info(e.getMessage());
			/*LOGGER.error("Exception in validateInputFileCollections() - FIRcvCollPostBOImpl: " + e.getMessage());
			exceptionMessage = "Exception Occured in validateInputFileCollections(): " + e.getMessage();
			isUserException = false;
			isFurtherProcessingReqd = false;
			fiExc = setExceptionCargo(exceptionMessage, FiConstants.FILE_SDU);
			fiRcvCollPostOutput = new FiRcvCollPostOutput();
			fiRcvCollPostOutput.setFiExceptionsCargo(fiExc);
			return fiRcvCollPostOutput;*/
		}

		/*validatedOutput.setvFiBatchReceiptsCargos(null);
		validatedOutput.setvFiBatchReceiptsCargos(validatedList);
		validatedOutput.setFiUnpostedReciptsCargos(fiUnposList);
		validatedOutput.setFiExceptionsForUnpostedList(fiExceptionsList);

		LOGGER.info("FIRcvCollPostBOImpl - END of validateInputFileCollections(): FiRcvCollPostOutput: " + validatedOutput);
		return validatedOutput;*/
		return inputRecord;
	}

private POSDetailRecord setValuesInReceiptsCargo(POSDetailRecord detailRecord, String receiptStatus, String countyCd, String reasonCd) {

	if (null != receiptStatus && !FiConstants.BLANK.equals(receiptStatus)) {
		detailRecord.setReceiptStatus(receiptStatus);
	}
	if (null != countyCd && !FiConstants.BLANK.equals(countyCd)) {
		detailRecord.setCountyCode(countyCd);
	}
	if (null != reasonCd && !FiConstants.BLANK.equals(reasonCd)) {
		detailRecord.setReasonCd(reasonCd); 
	}

	return detailRecord;
}

private UnpostedDetailRecord copyFromViewtoUnposted(POSDetailRecord detailRecord){
	UnpostedDetailRecord fiUnPOReciptsCargo = new UnpostedDetailRecord();

	fiUnPOReciptsCargo.setAmount(detailRecord.getPostReceiptAmount());
	fiUnPOReciptsCargo.setCaseNum(detailRecord.getPostCaseId());
	fiUnPOReciptsCargo.setEmpId(detailRecord.getPostEmpId());
	fiUnPOReciptsCargo.setMemberId(detailRecord.getPostMemberId());
	fiUnPOReciptsCargo.setMemberSsn(detailRecord.getPostMemberId());
	fiUnPOReciptsCargo.setPaymentMethod(detailRecord.getPostPayMethod());
	fiUnPOReciptsCargo.setPaymentSource(detailRecord.getPostSource());
	fiUnPOReciptsCargo.setSduScdNumber(detailRecord.getPostScdNumber());
	fiUnPOReciptsCargo.setCollDate(detailRecord.getPostColDate());

	log.info("FIRcvCollPostBOImpl - END of copyFromViewtoUnposted(): FiUnpostedReciptsCargo:" + fiUnPOReciptsCargo);

	return fiUnPOReciptsCargo;
}

}