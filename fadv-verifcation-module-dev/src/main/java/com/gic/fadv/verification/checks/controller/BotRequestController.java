package com.gic.fadv.verification.checks.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.checks.pojo.RootPOJO;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class BotRequestController {

	@PostMapping("/bot-request")
	public RootPOJO processBotRequest(@RequestBody RootPOJO rootPOJO) {
		return rootPOJO;
	}

}
