package com.gic.fadv.vendor.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileSystemException;
import org.springframework.stereotype.Service;

@Service
public interface VendorSftpConnectionService {

	boolean downloadFromSftp(String source, String destination);

	boolean checkIfFileExists(String path);

	void sfptEducationListener(String path, int currentDate);

	void sfptCriminalListener(String path, int currentDate);

	void sfptAddressListener(String path, int currentDate);

	boolean checkValidPath(String path);

	void createSftpDirectory(String completePath);

	boolean uploadFileTransferToSFTPServer(File convFile, String transferFileName, String componentName,
			String subFolder);

	void uploadFileToWindowsSharedPath(String remotePath, String localPath, String fileName) throws FileSystemException;

	void copyFileToAnotherPath(String remotePath, String localPath, String fileName) throws IOException;

}
