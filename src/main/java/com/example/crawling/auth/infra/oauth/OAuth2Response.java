package com.example.crawling.auth.infra.oauth;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();
}
