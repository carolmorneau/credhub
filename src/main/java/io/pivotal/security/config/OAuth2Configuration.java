package io.pivotal.security.config;

import io.pivotal.security.oauth.AuditOAuth2AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

@Configuration
@EnableResourceServer
@EnableWebSecurity
public class OAuth2Configuration extends ResourceServerConfigurerAdapter {

  @Autowired
  ResourceServerProperties resourceServerProperties;

  @PostConstruct
  public void init() {
    Assert.notNull(resourceServerProperties.getJwt().getKeyValue(), "Configuration property security.oauth2.resource.jwt.key-value must be set.");
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
    AuthenticationEntryPoint auditOAuth2AuthenticationEntryPoint = new AuditOAuth2AuthenticationEntryPoint();
    resources.resourceId(resourceServerProperties.getResourceId());
    resources.authenticationEntryPoint(auditOAuth2AuthenticationEntryPoint);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/info").permitAll()
        .antMatchers("/health").permitAll()
        .antMatchers("/api/v1/**").access("#oauth2.hasScope('credhub.read') and #oauth2.hasScope('credhub.write')");
  }
}