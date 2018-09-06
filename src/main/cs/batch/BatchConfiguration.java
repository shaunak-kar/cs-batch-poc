package batch;

import java.util.Map;

import javax.sql.DataSource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import dto.POSFileDTO;
import reader.POSFileReader;
import util.BatchUtils;
import writer.POSCollectionWriter;

@Configuration
@EnableBatchProcessing
@PropertySource("classpath:batch.properties")
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	@StepScope
	public ItemReader<String> posFileReader(@Value("#{jobParameters}") Map<String,String> jobParams) {
		//String fileId = "0681h0000001nniAAA";
		System.out.println("Params:" + jobParams);
		System.out.println("Reading File:" + jobParams.get("fileId"));

		RestTemplate restTemplate = new RestTemplate();

		// Body
		JSONObject request = new JSONObject();
		try {
			request.put("fileId", jobParams.get("fileId"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Header
		String oAuthToken = "OAuth "+BatchUtils.getAccessToken();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", oAuthToken);

		// Entity
		HttpEntity<String> reqEntity = new HttpEntity<String>(request.toString(), headers);
		
		return new POSFileReader(reqEntity, restTemplate);
	}

	/*
	 * TODO 1. Read POS1-SDU-YYYYMMDD-TIMESTAMP.csv file 2.
	 */
	//@Bean
	public FlatFileItemReader<POSFileMarshallingBean> reader() {

		return new FlatFileItemReaderBuilder<POSFileMarshallingBean>().name("posRecordReader")
				.resource(new ClassPathResource("POS1-SDU-20180829-1832.csv")).delimited()
				.names(new String[] { "postHeaderRecordType", "postFileSequenceNumber", "postTransmitDate",
						"postReceiptType", "postBatchDate", "postMemberId", "postScdNumber", "postShortName",
						"postCaseId", "postDefSsn", "postColDate", "postReceiptAmount", "postPayMethod", "postSource",
						"postEmpId", "postCheckNum", "postTrailerRecordType", "postRecordCount", "postTotalAmount" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<POSFileMarshallingBean>() {
					{
						setTargetType(POSFileMarshallingBean.class);
					}
				}).build();
	}

	@Bean
	public POSItemProcessor processor() {
		return new POSItemProcessor();
	}
	
	@Bean
	public ItemWriter<JSONObject> writeCollections(){
		return new POSCollectionWriter();
	}

	@Bean
	public JdbcBatchItemWriter<POSCollectionRecord> writer(DataSource dataSource) {
		System.out.println("Writing...");

		return new JdbcBatchItemWriterBuilder<POSCollectionRecord>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO people (first_name, last_name) VALUES (:name, :amount)").dataSource(dataSource)
				.build();
		
	}

	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("COLL-AD-HOC").incrementer(new RunIdIncrementer()).listener(listener).flow(step1)
				.end().build();
	}

	@Bean
	public Step step1(ItemWriter<JSONObject> writer) {
		return stepBuilderFactory.get("step1").<String, JSONObject>chunk(10)
				.reader(posFileReader(null)).processor(processor()).writer(writer).build();
	}
}