package batch;

public class POSTrailerRecords {
	private String postTrailerRecordType;
	private String postRecordCount;
	private String postTotalAmount;
	public String getPostTrailerRecordType() {
		return postTrailerRecordType;
	}
	public void setPostTrailerRecordType(String postTrailerRecordType) {
		this.postTrailerRecordType = postTrailerRecordType;
	}
	public String getPostRecordCount() {
		return postRecordCount;
	}
	public void setPostRecordCount(String postRecordCount) {
		this.postRecordCount = postRecordCount;
	}
	public String getPostTotalAmount() {
		return postTotalAmount;
	}
	public void setPostTotalAmount(String postTotalAmount) {
		this.postTotalAmount = postTotalAmount;
	}
	@Override
	public String toString() {
		return "PosTrailerPojo [postTrailerRecordType=" + postTrailerRecordType + ", postRecordCount=" + postRecordCount
				+ ", postTotalAmount=" + postTotalAmount + "]";
	}
	
	
}
