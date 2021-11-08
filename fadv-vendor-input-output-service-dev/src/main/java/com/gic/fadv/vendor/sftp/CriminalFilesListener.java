package com.gic.fadv.vendor.sftp;

import org.apache.commons.io.FilenameUtils;
import com.gic.fadv.vendor.output.service.VendorOutputServiceNew;
import com.github.drapostolos.rdp4j.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriminalFilesListener implements DirectoryListener, IoErrorListener, InitialContentListener {

	private VendorOutputServiceNew vendorOutputServiceNew;

	private static final Logger logger = LoggerFactory.getLogger(CriminalFilesListener.class);

	private String path;

	public CriminalFilesListener(String path, VendorOutputServiceNew vendorOutputServiceNew) {
		this.path = path;
		this.vendorOutputServiceNew = vendorOutputServiceNew;
	}

	public void fileAdded(FileAddedEvent event) {
		logger.info("New Criminal file Added : {}", event.getFileElement());
		String fileExtension = FilenameUtils.getExtension(event.getFileElement().toString());
		if (fileExtension.equalsIgnoreCase("xlsx")) {
			vendorOutputServiceNew.processVendorOutputRequest("criminal", event.getFileElement().toString(), this.path);
		}
	}

	public void fileRemoved(FileRemovedEvent event) {
		logger.info("Criminal file Removed : {}", event.getFileElement());
	}

	public void fileModified(FileModifiedEvent event) {
		logger.info("Criminal file Modified : {} ", event.getFileElement());
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