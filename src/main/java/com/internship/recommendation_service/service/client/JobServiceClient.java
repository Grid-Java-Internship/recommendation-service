package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.config.property.service.JobServiceConfig;
import com.internship.recommendation_service.config.property.service.ServiceNamesConfig;
import com.internship.recommendation_service.dto.external.JobDTO;
import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class JobServiceClient {
    private final ServiceClient serviceClient;
    private final ServiceNamesConfig serviceNamesConfig;
    private final JobServiceConfig jobServiceConfig;

    @Value("${security.feign.job-service.api-key}")
    private String jobApiKey;

    /**
     * Returns a Flux that emits a stream of JobDTO objects, representing all jobs
     * in the job service.
     *
     * @return a Flux that emits a stream of JobDTO objects, representing all jobs
     * in the job service
     */
    public Flux<JobDTO> getAllJobs() {
        String baseUrl = serviceNamesConfig.getJobService() + jobServiceConfig.getBaseUrl();
        String url = buildUrlToFetchAllJobs(baseUrl);

        LogUtil.info("Getting all jobs");
        return serviceClient.getFluxList(url, JobDTO.class, jobApiKey);
    }

    /**
     * Returns a URL that can be used to fetch all jobs in the job service.
     * <p>
     * The returned URL is built by adding the query parameters "page" and "size"
     * to the given base URL. The value of "page" is set to 0 and the value of
     * "size" is set to the maximum value of an integer.
     * <p>
     * The resulting URL can be used to fetch all jobs in the job service in a
     * single request.
     *
     * @param baseUrl the base URL of the job service
     * @return a URL that can be used to fetch all jobs in the job service
     */
    private String buildUrlToFetchAllJobs(String baseUrl) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", 0)
                .queryParam("size", Integer.MAX_VALUE)
                .encode()
                .toUriString();
    }
}
