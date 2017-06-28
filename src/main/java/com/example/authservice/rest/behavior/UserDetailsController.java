package com.example.authservice.rest.behavior;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserDetailsController {

	 @RequestMapping(path="/api/me", produces="application/json; charset=UTF-8", method=RequestMethod.GET)
	 public Principal user(Principal user) {
		 return user;
	 }
	
}
