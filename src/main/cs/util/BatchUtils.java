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
        String username = "csdemo@nextgen-cs.com";
        String password = "Deloitte.123bVtTgbUtNegHQYylq9tczWcU";
        String ClientId= "3MVG98im9TK34CUUPEErKNM6CLDHiNFt5rSE39XabQa.x6E.9mKOg.ys3KkuhEI8B2FEIpv6bb2j1fqDQkGTJ";
        String ClientSecret = "1127711847586731019";
        
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
		trailer.setPostTotalAmount(bean.getPostTotalAmount());
		detail.setPostBatchDate(bean.getPostBatchDate());
		detail.setPostReceiptAmount(bean.getPostReceiptAmount());
		detail.setPostMemberId(bean.getPostMemberId());
		detail.setPostPayMethod(bean.getPostPayMethod());
		detail.setPostSource(bean.getPostSource());
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
}
