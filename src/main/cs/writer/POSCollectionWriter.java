package writer;

import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import util.BatchUtils;

public class POSCollectionWriter implements ItemWriter<JSONObject>{

	@Override
	public void write(List<? extends JSONObject> str) throws Exception {
	
		RestTemplate restTemplate = new RestTemplate();
		String endpoint = "https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/apexrest/createreceipts/";
		//String endpoint= "https://next-gen-child-cs22--csdemo.cs60.my.salesforce.com/services/apexrest/createreceipts/";
		// Body
		String request = str.get(0).toString();
		System.out.println("Collections to Write: "+ request);

		// Header
		String oAuthToken = "OAuth "+ BatchUtils.getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", oAuthToken);

		// Entity
		HttpEntity<String> reqEntity = new HttpEntity<String>(request, headers);
		ResponseEntity<String> resp = restTemplate.postForEntity(endpoint, reqEntity, String.class);
		
		
		System.out.println("Response from Salesforce: "+ resp);
	}

}
