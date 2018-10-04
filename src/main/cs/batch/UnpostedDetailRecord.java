package batch;

public class UnpostedDetailRecord {

	private String seqNum;
	private String sduScdNumber;
	private String caseNum;
	private String memberId;
	private String paymentSource;
	private String paymentMethod;
	private String memberSsn;
	private String collDate;
	private String amount;
	private String checkEftNbr;
	private String empId;


   /**
    * Default constructor.
    */
	public UnpostedDetailRecord(){
		super();
	}
	

	public String getSeqNum() {
		return this.seqNum;
	}
	
	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}

	public String getSduScdNumber() {
		return this.sduScdNumber;
	}
	
	public void setSduScdNumber(String sduScdNumber) {
		this.sduScdNumber = sduScdNumber;
	}

	public String getCaseNum() {
		return this.caseNum;
	}
	
	public void setCaseNum(String caseNum) {
		this.caseNum = caseNum;
	}

	public String getMemberId() {
		return this.memberId;
	}
	
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getPaymentSource() {
		return this.paymentSource;
	}
	
	public void setPaymentSource(String paymentSource) {
		this.paymentSource = paymentSource;
	}

	public String getPaymentMethod() {
		return this.paymentMethod;
	}
	
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getMemberSsn() {
		return this.memberSsn;
	}
	
	public void setMemberSsn(String memberSsn) {
		this.memberSsn = memberSsn;
	}

	public String getCollDate() {
		return this.collDate;
	}
	
	public void setCollDate(String collDate) {
		this.collDate = collDate;
	}

	public String getAmount() {
		return this.amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCheckEftNbr() {
		return this.checkEftNbr;
	}
	
	public void setCheckEftNbr(String checkEftNbr) {
		this.checkEftNbr = checkEftNbr;
	}

	public String getEmpId() {
		return this.empId;
	}
	
	public void setEmpId(String empId) {
		this.empId = empId;
	}

	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("FiUnpostedRecipts:");
		sb.append("seqNum=");
		sb.append(seqNum);
		sb.append(",");
		sb.append("sduScdNumber=");
		sb.append(sduScdNumber);
		sb.append(",");
		sb.append("caseNum=");
		sb.append(caseNum);
		sb.append(",");
		sb.append("memberId=");
		sb.append(memberId);
		sb.append(",");
		sb.append("paymentSource=");
		sb.append(paymentSource);
		sb.append(",");
		sb.append("paymentMethod=");
		sb.append(paymentMethod);
		sb.append(",");
		sb.append("memberSsn=");
		sb.append(memberSsn);
		sb.append(",");
		sb.append("collDate=");
		sb.append(collDate);
		sb.append(",");
		sb.append("amount=");
		sb.append(amount);
		sb.append(",");
		sb.append("checkEftNbr=");
		sb.append(checkEftNbr);
		sb.append(",");
		sb.append("empId=");
		sb.append(empId);
		sb.append(",");
		sb.append(super.toString());
		return sb.toString();
	}	
		
}
