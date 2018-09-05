package batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
	@Autowired
    JobLauncher jobLauncher;
	
    @Autowired
    Job job;

    @GetMapping("/")
    public String index() throws Exception{
    	
        return "Collection Batch is running";
    }
    
    @GetMapping("/initiateCollection")
    public String initColl(@RequestParam(value="id") String posFileId) {
    	JobParameters jobParameters = new JobParametersBuilder().addString("fileId", posFileId)
				.toJobParameters();
    	try {
			jobLauncher.run(job, new JobParameters());
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			e.printStackTrace();
		}
    	
    	System.out.println("Initialized Collections for : " +posFileId);
        return "Child Support batch is good to go";
    }
}
