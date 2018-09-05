package batch;

public class POSCollectionRecord {
	private String name;
	private String amount;
	private String batch;
	private String receiptDate;
	private String receiptStatus;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}
	public String getReceiptDate() {
		return receiptDate;
	}
	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}
	public String getReceiptStatus() {
		return receiptStatus;
	}
	public void setReceiptStatus(String receiptStatus) {
		this.receiptStatus = receiptStatus;
	}
	@Override
	public String toString() {
		return "FiReceiptsPojo [name=" + name + ", amount=" + amount + ", batch=" + batch + ", receiptDate="
				+ receiptDate + ", receiptStatus=" + receiptStatus + "]";
	}
	
}
