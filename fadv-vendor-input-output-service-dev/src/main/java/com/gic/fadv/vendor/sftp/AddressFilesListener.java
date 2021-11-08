package com.gic.fadv.vendor.sftp;

import com.gic.fadv.vendor.output.service.VendorOutputServiceNew;
import com.github.drapostolos.rdp4j.*;
import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressFilesListener implements DirectoryListener, IoErrorListener, InitialContentListener {

	private VendorOutputServiceNew vendorOutputServiceNew;

	private static final Logger logger = LoggerFactory.getLogger(AddressFilesListener.class);

	private String path;

	public AddressFilesListener(String path, VendorOutputServiceNew vendorOutputServiceNew) {
		this.path = path;
		this.vendorOutputServiceNew = vendorOutputServiceNew;
	}

	public void fileAdded(FileAddedEvent event) {
		logger.info("New Address file Added : {}", event.getFileElement());
		String fileExtension = FilenameUtils.getExtension(event.getFileElement().toString());
		if (fileExtension.equalsIgnoreCase("xlsx")) {
			vendorOutputServiceNew.processVendorOutputRequest("address", event.getFileElement().toString(), this.path);
		}
	}

	public void fileRemoved(FileRemovedEvent event) {
		logger.info("Address file Removed : {}", event.getFileElement());
	}

	public void fileModified(FileModifiedEvent event) {
		logger.info("Address file Modified : {} ", event.getFileElement());
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