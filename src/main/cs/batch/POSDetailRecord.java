package batch;

public class POSDetailRecord {
	private String postReceiptType;
	private String postBatchDate;
	private String postMemberId;
	private String postScdNumber;
	private String postShortName;
	private String postCaseId;
	private String postDefSsn;
	private String postColDate;
	private String postReceiptAmount;
	private String postPayMethod;
	private String postSource;
	private String postEmpId;
	private String postCheckNum;
	private String reasonCd;
	private String receiptStatus;
	private String countyCode;
	private boolean isUnpostedRecord;
	
	
	public String getPostReceiptType() {
		return postReceiptType;
	}
	
	public void setPostReceiptType(String postReceiptType) {
		this.postReceiptType = postReceiptType;
	}
	
	public boolean getIsUnpostedRecord() {
		return isUnpostedRecord;
	}
	
	public void setIsUnpostedRecord(boolean isUnpostedRecord) {
		this.isUnpostedRecord = isUnpostedRecord;
	}
	
	public String getCountyCode() {
		return countyCode;
	}
	
	public void setCountyCode(String countyCode) {
		this.countyCode = countyCode;
	}
	
	public String getReceiptStatus() {
		return receiptStatus;
	}
	
	public void setReceiptStatus(String receiptStatus) {
		this.receiptStatus = receiptStatus;
	}
	
	public String getPostBatchDate() {
		return postBatchDate;
	}
	public void setPostBatchDate(String postBatchDate) {
		this.postBatchDate = postBatchDate;
	}
	
	
	public String getReasonCd() {
		return this.reasonCd;
	}
	
	public void setReasonCd(String reasonCd) {
		this.reasonCd = reasonCd;
	}
	
	public String getPostMemberId() {
		return postMemberId;
	}
	public void setPostMemberId(String postMemberId) {
		this.postMemberId = postMemberId;
	}
	public String getPostScdNumber() {
		return postScdNumber;
	}
	public void setPostScdNumber(String postScdNumber) {
		this.postScdNumber = postScdNumber;
	}
	public String getPostShortName() {
		return postShortName;
	}
	public void setPostShortName(String postShortName) {
		this.postShortName = postShortName;
	}
	public String getPostCaseId() {
		return postCaseId;
	}
	public void setPostCaseId(String postCaseId) {
		this.postCaseId = postCaseId;
	}
	public String getPostDefSsn() {
		return postDefSsn;
	}
	public void setPostDefSsn(String postDefSsn) {
		this.postDefSsn = postDefSsn;
	}
	public String getPostColDate() {
		return postColDate;
	}
	public void setPostColDate(String postColDate) {
		this.postColDate = postColDate;
	}
	public String getPostReceiptAmount() {
		return postReceiptAmount;
	}
	public void setPostReceiptAmount(String postReceiptAmount) {
		this.postReceiptAmount = postReceiptAmount;
	}
	public String getPostPayMethod() {
		return postPayMethod;
	}
	public void setPostPayMethod(String postPayMethod) {
		this.postPayMethod = postPayMethod;
	}
	public String getPostSource() {
		return postSource;
	}
	public void setPostSource(String postSource) {
		this.postSource = postSource;
	}
	public String getPostEmpId() {
		return postEmpId;
	}
	public void setPostEmpId(String postEmpId) {
		this.postEmpId = postEmpId;
	}
	public String getPostCheckNum() {
		return postCheckNum;
	}
	public void setPostCheckNum(String postCheckNum) {
		this.postCheckNum = postCheckNum;
	}
	@Override
	public String toString() {
		return "PosTrailerPojo [postReceiptType=" + postReceiptType + ", postBatchDate=" + postBatchDate
				+ ", postMemberId=" + postMemberId + ", postScdNumber=" + postScdNumber + ", postShortName="
				+ postShortName + ", postCaseId=" + postCaseId + ", postDefSsn=" + postDefSsn + ", postColDate="
				+ postColDate + ", postReceiptAmount=" + postReceiptAmount + ", postPayMethod=" + postPayMethod
				+ ", postSource=" + postSource + ", postEmpId=" + postEmpId + ", postCheckNum=" + postCheckNum + "receiptStatus = "+receiptStatus + " CountyCode = " + countyCode +"isUnpostedRecord =  "+ isUnpostedRecord + "]";
	}
}
