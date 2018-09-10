package reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import dto.POSFileDTO;

public class POSFileReader implements ItemReader<String>{
	
	private final HttpEntity<String> requestEntity;
	private final RestTemplate restTemplate;
	private int nextFileIndex = 0;
	private List<String> posFileContent = new ArrayList<>();
	private final String url = "https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/apexrest/fetchfilecontent/";

	@Override
	public String read() throws Exception {
		String nextPosFile;
		if(posFileContent.isEmpty()) {
			this.getPOSFileContent();
		}
		
		nextPosFile = null;
		
		if (nextFileIndex < posFileContent.size()) {
			nextPosFile = posFileContent.get(nextFileIndex);
            nextFileIndex++;
        }
				
		return nextPosFile;
	}

	public POSFileReader(HttpEntity<String> requestEntity, RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.requestEntity = requestEntity;
		nextFileIndex = 0;
	}
	
	//Eventually change this to accept multiple fileIds and return multiple file contents
	private List<String> getPOSFileContent() {
		ResponseEntity<String> response = restTemplate.postForEntity(this.url, this.requestEntity,
				String.class);
		try {
			JSONObject obj  = new JSONObject(response.getBody());
			System.out.println(obj.toString());
			Iterator<String> i = obj.keys();
			while(i.hasNext()) {
					posFileContent.add(obj.getString(i.next()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return posFileContent;
	}
	
}
