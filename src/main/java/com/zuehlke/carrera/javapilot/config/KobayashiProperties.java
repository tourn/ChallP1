package com.zuehlke.carrera.javapilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Simulator Settings
 */
@ConfigurationProperties(prefix="javapilot") // loaded from /resources/application.yml
public class KobayashiProperties {
    private String relayUrl;
    private String name;
    private String accessCode;


    public String getRelayUrl() {
        return relayUrl;
    }
    public String getName() {
        return name;
    }

    public void setRelayUrl(String relayUrl) {
        this.relayUrl = relayUrl;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }
}