package com.gic.fadv.verification.docs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.docs.pojo.ResponseAssociateDocs;
import com.gic.fadv.verification.docs.service.DocsService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class DocsController {
	
	@Autowired
	private DocsService docsService;
	
	@PostMapping("/associate_docs/{checkId}")
	public ResponseEntity<List<String>> getAssociateDocsUrl(@RequestBody ResponseAssociateDocs responseAssociateDocs,@PathVariable String checkId ) {
		
		return new ResponseEntity<>(docsService.getAssociateDocsUrlData(responseAssociateDocs, checkId), HttpStatus.OK) ;
	
	}

}
