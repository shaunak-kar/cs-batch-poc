package batch;

import java.util.List;

public class POSSDURecord {
	private POSHeaderRecord posHeaderRecord;
	private POSTrailerRecords posTrailerRecord;
	private List<POSDetailRecord> posDetailRecord;
	
	public POSHeaderRecord getPosHeaderRecord() {
		return posHeaderRecord;
	}
	public void setPosHeaderRecord(POSHeaderRecord posHeaderRecord) {
		this.posHeaderRecord = posHeaderRecord;
	}
	public POSTrailerRecords getPosTrailerRecord() {
		return posTrailerRecord;
	}
	public void setPosTrailerRecord(POSTrailerRecords posTrailerRecord) {
		this.posTrailerRecord = posTrailerRecord;
	}
	public List<POSDetailRecord> getPosDetailRecord() {
		return posDetailRecord;
	}
	public void setPosDetailRecord(List<POSDetailRecord> posDetailRecord) {
		this.posDetailRecord = posDetailRecord;
	}
	
	
}
