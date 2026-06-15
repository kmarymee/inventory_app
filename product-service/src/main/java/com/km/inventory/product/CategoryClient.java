package com.km.inventory.product;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CategoryClient {

    private final RestClient restClient;

    public CategoryClient(RestClient cateoryRestClient) {
        this.restClient = cateoryRestClient;
    }

    public boolean categoryExists(Long id) {
        return restClient.get()
            .uri("/categories/{id}", id)
            .retrieve()
            .onStatus(status -> status.value() == 404, (request,response) -> {}) //Ignore 404s
            .toBodilessEntity()
            .getStatusCode()
            .is2xxSuccessful();
    }
}
