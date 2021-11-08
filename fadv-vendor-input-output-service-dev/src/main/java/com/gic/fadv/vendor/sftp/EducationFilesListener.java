package com.gic.fadv.vendor.sftp;

import org.apache.commons.io.FilenameUtils;
import com.gic.fadv.vendor.output.service.VendorOutputServiceNew;
import com.github.drapostolos.rdp4j.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EducationFilesListener implements DirectoryListener, IoErrorListener, InitialContentListener {

	private VendorOutputServiceNew vendorOutputServiceNew;

	private static final Logger logger = LoggerFactory.getLogger(EducationFilesListener.class);

	private String path;

	public EducationFilesListener(String path, VendorOutputServiceNew vendorOutputServiceNew) {
		this.path = path;
		this.vendorOutputServiceNew = vendorOutputServiceNew;
	}

	public void fileAdded(FileAddedEvent event) {
		logger.info("New Education file Added : {}", event.getFileElement());
		String fileExtension = FilenameUtils.getExtension(event.getFileElement().toString());
		if (fileExtension.equalsIgnoreCase("zip")) {
			vendorOutputServiceNew.processVendorOutputRequest("education", event.getFileElement().toString(),
					this.path);
		}
	}

	public void fileRemoved(FileRemovedEvent event) {
		logger.info("Education file Removed : {}", event.getFileElement());
	}

	public void fileModified(FileModifiedEvent event) {
		logger.info("Education file Modified : {} ", event.getFileElement());
	}

	public void ioErrorCeased(IoErrorCeasedEvent event) {
		logger.info("I/O error ceased.");
	}

	public void ioErrorRaised(IoErrorRaisedEvent event) {
		logger.info("I/O error raised!");
	}

	public void initialContent(InitialContentEvent event) {
		logger.info("initial Content: ^");
	}
}