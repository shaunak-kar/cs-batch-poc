package batch;

public class POSHeaderRecord {
	private String postHeaderRecordType;
	private String postFileSequenceNumber;
	private String postTransmitDate;
	public String getPostHeaderRecordType() {
		return postHeaderRecordType;
	}
	public void setPostHeaderRecordType(String postHeaderRecordType) {
		this.postHeaderRecordType = postHeaderRecordType;
	}
	public String getPostFileSequenceNumber() {
		return postFileSequenceNumber;
	}
	public void setPostFileSequenceNumber(String postFileSequenceNumber) {
		this.postFileSequenceNumber = postFileSequenceNumber;
	}
	public String getPostTransmitDate() {
		return postTransmitDate;
	}
	public void setPostTransmitDate(String postTransmitDate) {
		this.postTransmitDate = postTransmitDate;
	}
	@Override
	public String toString() {
		return "PostHeaderPojo [postHeaderRecordType=" + postHeaderRecordType + ", postFileSequenceNumber="
				+ postFileSequenceNumber + ", postTransmitDate=" + postTransmitDate + "]";
	}
	

 

	 
}
