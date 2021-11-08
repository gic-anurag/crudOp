package com.gic.fadv.verification.word.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.word.pojo.WordFileInputPOJO;

@Service
public interface WordDocumentService {

	String handleSimpleDoc(WordFileInputPOJO wordFileInputPOJO)throws InvalidFormatException, IOException, URISyntaxException;

}
