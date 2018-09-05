package reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import dto.POSFileDTO;

public class POSFileReader implements ItemReader<POSFileDTO>{
	
	private final HttpEntity<String> requestEntity;
	private final RestTemplate restTemplate;
	private int nextFileIndex;
	private POSFileDTO posFileContent;
	private final String url = "https://next-gen-child-cs22--cs.cs79.my.salesforce.com/services/apexrest/fetchfilecontent/";

	@Override
	public POSFileDTO read() throws Exception {
		
		if(posFileContent == null) {
			this.posFileContent = this.getPOSFileContent();
		}
		// TODO Auto-generated method stub
		return this.posFileContent;
	}

	public POSFileReader(HttpEntity<String> requestEntity, RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.requestEntity = requestEntity;
		nextFileIndex = 0;
	}
	
	//Eventually change this to accept multiple fileIds and return multiple file contents
	private POSFileDTO getPOSFileContent() {
		ResponseEntity<POSFileDTO> response = restTemplate.exchange(this.url, HttpMethod.POST, this.requestEntity,
				POSFileDTO.class);
		posFileContent = response.getBody();
		System.out.println("POS FileContent:" + posFileContent.getContent());
		return posFileContent;
	}
	
}
