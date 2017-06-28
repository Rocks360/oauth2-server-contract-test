package com.example.authservice.rest.behavior;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import com.example.authservice.conf.AuthserverConfig;
import com.example.authservice.conf.AuthserverConfig.AuthServerConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
public abstract class UserDetailsControllerTestBase {

	public static Logger log =  LoggerFactory.getLogger(UserDetailsControllerTestBase.class);
	
	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mvc;

	@Autowired
	FilterChainProxy springSecurityFilterChain;
	
	
	@Mock SecurityContext mockSecurityContext;
	
    @Before
    public void setUp() {
     	RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

     	//RestAssured.baseURI="http://localhost";
     	//RestAssured.port = port;
    		
	   	 mvc = MockMvcBuilders
	             .webAppContextSetup(webApplicationContext)
	             .defaultRequest(get("/api/me").with(user("user").roles("USER")))
	             .apply(springSecurity(springSecurityFilterChain))
	             .build();
	   	RestAssuredMockMvc.mockMvc(mvc);

    }

    public String oauth(){
    		String token = "notValidToken";
    		try {
			token = validAccessToken();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    	 	return "Bearer " + token;
    }
    
    public void assertIsNull(Object value){
    		assertThat(value, is(nullValue()));
    }
    
    private String validAccessToken() throws Exception {  
    	String username = "user";
    	String password = "password";
    	
		MockHttpServletResponse response = mvc
            .perform(post("/oauth/token")
                    .header("Authorization", "Basic "
                           + new String(Base64Utils.encode(("client:clientsecret")
                            .getBytes())))
                    .param("username", username)
                    .param("password", password)
                    .param("grant_type", "password"))
            .andDo(print())
            .andReturn().getResponse();
    return new ObjectMapper()
            .readValue(response.getContentAsByteArray(), OAuthToken.class)
            .accessToken;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class OAuthToken {
		@JsonProperty("access_token")
	    public String accessToken;
	}
}
