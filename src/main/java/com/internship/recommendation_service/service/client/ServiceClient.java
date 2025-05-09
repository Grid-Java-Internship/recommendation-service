package com.internship.recommendation_service.service.client;

import com.internship.recommendation_service.util.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceClient {
    private final WebClient webClient;

    /**
     * Sends a GET request to the specified URL and retrieves a single object of the specified response type.
     *
     * @param url          the URL to send the GET request to
     * @param responseType the class type of the response expected from the URL
     * @param <T>          the type of the response object
     * @return a Mono that emits the response object of type T
     */
    public <T> Mono<T> getMonoObject(String url, Class<T> responseType, String apiKey) {
        LogUtil.info("GET request to URL: {}", url);

        return webClient.get()
                .uri(url)
                .header("X-API-KEY", apiKey)
                .retrieve()
                .bodyToMono(responseType)
                .doOnEach(signal -> {
                    if (signal.isOnNext()) {
                        LogUtil.info("Response: {} for URL: {}", signal.get(), url);
                    }
                })
                .doOnError(e -> LogUtil.error("GET request to {} failed", url, e));
    }

    /**
     * Sends a GET request to the specified URL and retrieves a Flux of objects of the specified response type.
     *
     * @param url          the URL to send the GET request to
     * @param responseType the class type of the response expected from the URL
     * @param <T>          the type of the response objects
     * @return a Flux that emits the response objects of type T
     */
    public <T> Flux<T> getFluxList(String url, Class<T> responseType, String apiKey) {
        LogUtil.info("GET request to URL: {}", url);

        return webClient.get()
                .uri(url)
                .header("X-API-KEY", apiKey)
                .retrieve()
                .bodyToFlux(responseType)
                .doOnEach(signal -> {
                    if (signal.isOnNext()) {
                        LogUtil.info("Response: {} for URL: {}", signal.get(), url);
                    }
                })
                .doOnError(e -> LogUtil.error("GET request to {} failed", url, e));
    }

    /**
     * Sends a GET request to the specified URL and retrieves a Mono that emits a list of objects of the specified response type.
     *
     * @param url          the URL to send the GET request to
     * @param responseType the class type of the response expected from the URL
     * @param <T>          the type of the response objects
     * @return a Mono that emits the response objects of type T
     */
    public <T> Mono<List<T>> getMonoList(String url, Class<T> responseType, String apiKey) {
        return getFluxList(url, responseType, apiKey)
                .collectList()
                .onErrorReturn(List.of());
    }
}
