package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import batch.POSDetailRecord;
import batch.POSFileMarshallingBean;
import batch.POSHeaderRecord;
import batch.POSSDURecord;
import batch.POSTrailerRecords;

public class BatchUtils {
	
	private String accessToken;
	
	
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public static String getAccessToken() {
		
		String endpoint="https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/oauth2/token";
		//String endpoint= "https://next-gen-child-cs22--csdemo.cs60.my.salesforce.com/services/oauth2/token";
		
		// Dev Org Details
		
			 String username = "pradem@nextgen-cs.com.cs";
        String password = "Childsupport@123zhAGKz0mr5QgcsH6YYLVayy6f";
        String ClientId= "3MVG98im9TK34CUUPEErKNM6CLDHiNFt5rSE39XabQa.x6E.9mKOg.ys3KkuhEI8B2FEIpv6bb2j1fqDQkGTJ";
        String ClientSecret = "1127711847586731019";
		
		// Demo Org Details.                   
       /* String username = "csdemo@nextgen-csdemo.com";
        String password = "Hello123PDYU40r6UuKtSbGM5qiQSGLy";
        String ClientId= "3MVG9oZtFCVWuSwMo_8Ij7qDkIOa6TaR.qMN5Mi.Bj.Mb_WYODxK8_RI0iLnCpjwGxFrlCEhZOOW4c55NKPrR";
        String ClientSecret = "6855908894036373949";
        */
        //Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
         
        //Body
        String requestBody = "grant_type=password" + 
                "&client_id=" + ClientId + 
                "&client_secret=" + ClientSecret + 
                "&username=" + username +
                "&password=" + password;
        
        // Entity
		HttpEntity<String> reqEntity = new HttpEntity<String>(requestBody, headers);
		
		RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = restTemplate.postForEntity(endpoint, reqEntity, String.class);
        String accessToken = "";
		try {
			JSONObject respJSON = new JSONObject(resp.getBody());
			accessToken = (String) respJSON.get("access_token");
			System.out.println("Access Token:" +accessToken);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        return accessToken;
	}
	
	public static POSSDURecord getSDURecordFromMarshallingBean(POSFileMarshallingBean bean) {
		POSSDURecord sdu = new POSSDURecord();
		POSHeaderRecord header  = new POSHeaderRecord();
		POSTrailerRecords trailer = new POSTrailerRecords();
		POSDetailRecord detail  = new POSDetailRecord();
		List<POSDetailRecord> lstDetail = new ArrayList<>();
		
		header.setPostFileSequenceNumber(bean.getPostFileSequenceNumber());
		header.setPostTransmitDate(bean.getPostTransmitDate());
		header.setPostHeaderRecordType(bean.getPostHeaderRecordType());
		trailer.setPostTotalAmount(bean.getPostTotalAmount());
		detail.setPostBatchDate(bean.getPostBatchDate());
		detail.setPostReceiptAmount(bean.getPostReceiptAmount());
		detail.setPostMemberId(bean.getPostMemberId());
		detail.setPostPayMethod(bean.getPostPayMethod());
		detail.setPostSource(bean.getPostSource());
		detail.setPostReceiptType(bean.getPostReceiptType());
		detail.setPostScdNumber(bean.getPostScdNumber());
		detail.setPostShortName(bean.getPostShortName());
		detail.setPostCaseId(bean.getPostCaseId());
		detail.setPostDefSsn(bean.getPostDefSsn());
		detail.setPostColDate(bean.getPostColDate());
		detail.setPostEmpId(bean.getPostEmpId());
		detail.setPostCheckNum(bean.getPostCheckNum());
		trailer.setPostRecordCount(bean.getPostRecordCount());	
		 trailer.setPostTrailerRecordType(bean.getPostTrailerRecordType());
		 trailer.setPostTotalAmount(bean.getPostTotalAmount());
		
		lstDetail.add(detail);
		
		sdu.setPosHeaderRecord(header);
		sdu.setPosTrailerRecord(trailer);
		sdu.setPosDetailRecord(lstDetail);
		return sdu;
	}
	
	public static String formatDate(String yyyyMMdd) {
		String pattern = "yyyyMMdd";
		String pattern2 = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(pattern2);
		Date date;
		try {
			date = simpleDateFormat.parse(yyyyMMdd);
			return simpleDateFormat2.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static ResponseEntity<String> getDataFromRestCall(String endpoint,String requestBody){
		
		RestTemplate restTemplate = new RestTemplate();
		// Header
		
		System.out.println("Entered--Rest-Call");
		String oAuthToken = "OAuth "+ BatchUtils.getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", oAuthToken);
		System.out.println("Access token verified");
		
		// Entity
		HttpEntity<String> reqEntity = new HttpEntity<String>(requestBody, headers);
		ResponseEntity<String> resp = restTemplate.postForEntity(endpoint, reqEntity, String.class);
		
		
		System.out.println("Response: "+ resp);
		return resp;
		
	}
}
