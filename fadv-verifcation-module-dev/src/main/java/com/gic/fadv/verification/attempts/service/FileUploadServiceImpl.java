package com.gic.fadv.verification.attempts.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gic.fadv.verification.attempts.model.AttemptUploadDocument;
import com.gic.fadv.verification.attempts.model.ComponentDocumentType;
import com.gic.fadv.verification.attempts.repository.AttemptUploadDocumentRepository;
import com.gic.fadv.verification.attempts.repository.ComponentDocumentTypeRepository;
import com.gic.fadv.verification.utility.Utility;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class FileUploadServiceImpl implements FileUploadService {

	private static final String FILE_SEPERATOR = "/";

	@Value("${local.file.upload.location}")
	public String uploadDir;
	
	@Value("${remote.shared.file.path}")
	private String remoteSharedFilePath;

	@Autowired
	private ComponentDocumentTypeRepository componentDocumentTypeRepository;

	@Autowired
	private AttemptUploadDocumentRepository attemptUploadDocumentRepository;
	
	@Autowired
	private ServletContext servletContext;

	private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

	public HttpStatus uploadFile(MultipartFile fileName, AttemptUploadDocument attemptUploadDocument) {

		try {
			String fileNameStr = StringUtils.cleanPath(fileName.getOriginalFilename());
			String remotePath = remoteSharedFilePath + FILE_SEPERATOR + attemptUploadDocument.getCheckid() + FILE_SEPERATOR;
			
			File remoteFile = new File(remotePath);
			
			logger.info("remoteFile : {}", remoteFile.getAbsolutePath());

			String fileNameNew = FilenameUtils.getBaseName(fileNameStr);
			String extension = FilenameUtils.getExtension(fileNameStr);
			logger.info("fileNameNew : {}", fileNameNew);

			String attemptFileName = fileNameNew + "_" + attemptUploadDocument.getCheckid() + "." + extension;
			attemptUploadDocument.setDocumentPath(remoteFile.getAbsolutePath());
			attemptUploadDocument.setFileName(attemptFileName);

			if (attemptUploadDocument.getComponentDocumentid() != 0) {
				Optional<ComponentDocumentType> componentDocumentType = componentDocumentTypeRepository
						.findById(attemptUploadDocument.getComponentDocumentid());
				if (componentDocumentType.isPresent()) {
					attemptFileName = fileNameNew + "_" + componentDocumentType.get().getDocumentType()
							+ "_" + attemptUploadDocument.getCheckid() + "." + extension;
					attemptUploadDocument.setFileName(attemptFileName);
				}
			}

			copyFileToAnotherPath(remotePath, fileName, attemptFileName);

			attemptUploadDocumentRepository.save(attemptUploadDocument);
			return HttpStatus.CREATED;
		} catch (Exception e) {
			logger.error("Exception while saving upload docs : {}", e.getMessage());
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
	
	public void copyFileToAnotherPath(String remotePath, MultipartFile localPath, String fileNameStr) throws IOException {

		String copyPath = remotePath + fileNameStr;

		logger.info("Source path : {} ", localPath);
		logger.info("Destination path : {}", copyPath);

		File destFile = new File(copyPath);
		
		if (!destFile.exists()) {
			destFile.mkdirs();
		}
		
		Files.copy(localPath.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	}
	
	@Override
	public ResponseEntity<InputStreamResource> downloadUploadedFile(Long id) {
		Optional<AttemptUploadDocument> attemptUploadDocumentOpt = attemptUploadDocumentRepository.findById(id);
		if (attemptUploadDocumentOpt.isPresent()) {
			AttemptUploadDocument attemptUploadDocument = attemptUploadDocumentOpt.get();
			String filePath = attemptUploadDocument.getDocumentPath() + File.separator
					+ attemptUploadDocument.getFileName();

			logger.info("filepath : {}", filePath);

			File file = new File(filePath);
			InputStreamResource resource;
			try {
				resource = new InputStreamResource(new FileInputStream(file));

				MediaType mediaType = Utility.getMediaTypeForFileName(this.servletContext,
						attemptUploadDocument.getFileName());
				
				logger.info("mediaType : {}", mediaType);

				return ResponseEntity.ok()
						// Content-Disposition
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
						// Content-Type
						.contentType(mediaType)
						// Content-Length
						.contentLength(file.length()) //
						.body(resource);
			} catch (FileNotFoundException e) {
				logger.info("Exception while fetching file : {}", e.getMessage());
			}
		}

		return ResponseEntity.badRequest().body(null);
	}
}
