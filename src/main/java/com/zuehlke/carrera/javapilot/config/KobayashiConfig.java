package com.zuehlke.carrera.javapilot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KobayashiProperties.class})  // loaded from /resources/application.yml
public class KobayashiConfig {

}
