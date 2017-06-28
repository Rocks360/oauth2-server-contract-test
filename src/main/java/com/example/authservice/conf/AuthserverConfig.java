package com.example.authservice.conf;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


@EnableWebSecurity
public class AuthserverConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
		
	@Bean
	@Qualifier("tokenServices")
	@Primary
	public DefaultTokenServices tokenServices() {
        		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
	        defaultTokenServices.setTokenStore(tokenStore());
	        return defaultTokenServices;
	}
	
	
	@Bean
	@Qualifier("tokenStore")
	public TokenStore tokenStore() {
		return new JwtTokenStore(jwtAccessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		
		converter.setSigningKey("1234");
		converter.setVerifierKey("1234");
		return converter;
	}
	
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
		
	    @Override
	    protected MethodSecurityExpressionHandler createExpressionHandler() {
	        return new OAuth2MethodSecurityExpressionHandler();
	    }
	}
	
	@Configuration
	@EnableResourceServer
	@Order(1)
	public static class RestApiResourceConfig extends ResourceServerConfigurerAdapter {
		
		private static final String ACCOUNT_RESOURCE_ID = "accountservice";
	
		@Autowired
		private DefaultTokenServices tokenServices;
		
		@Override
		public void configure(ResourceServerSecurityConfigurer resource) {

			resource.resourceId(ACCOUNT_RESOURCE_ID);
			resource.tokenServices(tokenServices);
		}
	
		@Override
		public void configure(HttpSecurity http) throws Exception {
			
			http
			  .antMatcher("/api/*")
			  	.authorizeRequests()
			  	.antMatchers("/account/**").hasRole("USER")
			  	.antMatchers(HttpMethod.GET, "/account/me").fullyAuthenticated()
	          .anyRequest().authenticated();
		}
	}
	
	@Configuration
	@EnableAuthorizationServer
	public static class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;
		
		
		@Autowired
		private JwtAccessTokenConverter jwtAccessTokenConverter;

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
				clients.inMemory().withClient("client")
						.secret("clientsecret")
						.autoApprove(true)
						.accessTokenValiditySeconds(120)
						.authorizedGrantTypes("password")
						.resourceIds("accountservice")
						.scopes("read", "write");
		}

		
		
		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints)
				throws Exception {
			
			TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		    tokenEnhancerChain.setTokenEnhancers(
		      asList(jwtAccessTokenConverter));
		    endpoints.authenticationManager(authenticationManager)
		    			.accessTokenConverter(jwtAccessTokenConverter);
		 
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer)
				throws Exception {
			oauthServer.allowFormAuthenticationForClients();
			oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess(
					"isAuthenticated()");
		
		}
	}
	
	@Configuration   
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
		  
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.authorizeRequests()
					.anyRequest().authenticated();
		}
			
		@Override
		public void configure(AuthenticationManagerBuilder auth) throws Exception {
		    auth.inMemoryAuthentication().withUser("user")
		    										.password("password")
		    										.roles("USER");
		}
	}
}
