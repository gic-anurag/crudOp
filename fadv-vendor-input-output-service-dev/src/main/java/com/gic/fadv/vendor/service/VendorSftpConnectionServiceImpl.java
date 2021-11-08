package com.gic.fadv.vendor.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gic.fadv.vendor.output.service.VendorOutputServiceNew;
import com.gic.fadv.vendor.sftp.AddressFilesListener;
import com.gic.fadv.vendor.sftp.CriminalFilesListener;
import com.gic.fadv.vendor.sftp.EducationFilesListener;
import com.gic.fadv.vendor.sftp.SFtpDirectory;
import com.github.drapostolos.rdp4j.DirectoryPoller;
import com.github.drapostolos.rdp4j.spi.PolledDirectory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
public class VendorSftpConnectionServiceImpl implements VendorSftpConnectionService {

	@Autowired
	private VendorOutputServiceNew vendorOutputServiceNew;

	@Value("${secret.key.user}")
	private String keyAccessUser;
	@Value("${secret.key.password}")
	private String keyAccessPassword;
	@Value("${secret.key.ip}")
	private String keyAccessIP;
	@Value("${secret.key.port}")
	private Integer keyAccessPort;

	@Value("${server.sftp.input.address.file.path}")
	private String fileUploadPathAddress;
	@Value("${server.sftp.input.education.file.path}")
	private String fileUploadPathEducation;
	@Value("${server.sftp.input.criminal.file.path}")
	private String fileUploadPathCriminal;

	private static final Logger logger = LoggerFactory.getLogger(VendorSftpConnectionServiceImpl.class);

	/**
	 * This method create the AWS connection
	 * 
	 * @param jsch
	 * @return
	 * @throws JSchException
	 */
	private Session createSFTPConnection(JSch jsch) throws JSchException {
		Session session;
		session = jsch.getSession(keyAccessUser, keyAccessIP, keyAccessPort);
		session.setPassword(keyAccessPassword);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		return session;
	}

	@Override
	public boolean downloadFromSftp(String source, String destination) {

		JSch jsch = new JSch();
		Session session = null;
		try {
			session = createSFTPConnection(jsch);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.get(source, destination);
			sftpChannel.exit();
			session.disconnect();
		} catch (JSchException | SftpException e) {
			logger.error("Exception while downloading file : {} from SFTP : {}", source, e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean checkIfFileExists(String path) {
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = createSFTPConnection(jsch);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.ls(path);
			sftpChannel.exit();
			session.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void sfptAddressListener(String path, int currentDate) {

		logger.info("monitoring address directory : {}", path);
		PolledDirectory polledDirectory = new SFtpDirectory(keyAccessIP, path, keyAccessUser, keyAccessPassword,
				keyAccessPort);

		DirectoryPoller dp = DirectoryPoller.newBuilder().addPolledDirectory(polledDirectory)
				.addListener(new AddressFilesListener(path, vendorOutputServiceNew))
				.enableParallelPollingOfDirectories().setPollingInterval(10, TimeUnit.SECONDS).start();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
		int newDate = Integer.parseInt(simpleDateFormat.format(new Date()));

		while (currentDate == newDate) {
			newDate = Integer.parseInt(simpleDateFormat.format(new Date()));
		}

		dp.stop();
	}

	@Override
	public void sfptEducationListener(String path, int currentDate) {

		logger.info("monitoring education directory : {}", path);
		PolledDirectory polledDirectory = new SFtpDirectory(keyAccessIP, path, keyAccessUser, keyAccessPassword,
				keyAccessPort);

		DirectoryPoller dp = DirectoryPoller.newBuilder().addPolledDirectory(polledDirectory)
				.addListener(new EducationFilesListener(path, vendorOutputServiceNew))
				.enableParallelPollingOfDirectories().setPollingInterval(10, TimeUnit.SECONDS).start();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
		int newDate = Integer.parseInt(simpleDateFormat.format(new Date()));

		while (currentDate == newDate) {
			newDate = Integer.parseInt(simpleDateFormat.format(new Date()));
		}

		dp.stop();
	}

	@Override
	public void sfptCriminalListener(String path, int currentDate) {

		logger.info("monitoring criminal directory : {}", path);
		PolledDirectory polledDirectory = new SFtpDirectory(keyAccessIP, path, keyAccessUser, keyAccessPassword,
				keyAccessPort);

		DirectoryPoller dp = DirectoryPoller.newBuilder().addPolledDirectory(polledDirectory)
				.addListener(new CriminalFilesListener(path, vendorOutputServiceNew))
				.enableParallelPollingOfDirectories().setPollingInterval(10, TimeUnit.SECONDS).start();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
		int newDate = Integer.parseInt(simpleDateFormat.format(new Date()));

		while (currentDate == newDate) {
			newDate = Integer.parseInt(simpleDateFormat.format(new Date()));
		}
		dp.stop();
	}

	@Override
	public boolean checkValidPath(String path) {
		boolean isValid = false;
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = createSFTPConnection(jsch);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.cd(path);
			isValid = true;
			sftpChannel.exit();
			session.disconnect();
		} catch (Exception e) {
			isValid = false;
		}
		return isValid;
	}

	@Override
	public void createSftpDirectory(String completePath) {
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = createSFTPConnection(jsch);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			logger.info("Creating Directory...");
			String[] complPath = completePath.split("/");
			sftpChannel.cd("/");
			for (String folder : complPath) {
				if (folder.length() > 0) {
					try {
						logger.info("Current Directory : {}", sftpChannel.pwd());
						sftpChannel.cd(folder);
					} catch (SftpException e2) {
						sftpChannel.mkdir(folder);
						sftpChannel.cd(folder);
					}
				}
			}
			logger.info("Current Dir : {}", sftpChannel.pwd());

			sftpChannel.exit();
			session.disconnect();
		} catch (JSchException e) {
			logger.error(e.getMessage(), e);
		} catch (SftpException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean uploadFileTransferToSFTPServer(File convFile, String transferFileName, String componentName,
			String subFolder) {
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = createSFTPConnection(jsch);
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			String uploadedPath = null;
			String tranferPath = null;
			if (componentName.equalsIgnoreCase("Education")) {
				uploadedPath = fileUploadPathEducation + subFolder;
				tranferPath = uploadedPath + transferFileName;
			} else if (componentName.equalsIgnoreCase("Address")) {
				uploadedPath = fileUploadPathAddress + subFolder;
				tranferPath = uploadedPath + transferFileName;
			} else {
				uploadedPath = fileUploadPathCriminal + subFolder;
				tranferPath = uploadedPath + transferFileName;
			}
			/*
			 * Logic For Creating folder if not exist
			 */
			String[] folders = uploadedPath.split("/");
			for (String folder : folders) {
				if (StringUtils.isNotBlank(folder)) {
					try {
						sftpChannel.cd(folder);
					} catch (SftpException e) {
						sftpChannel.mkdir(folder);
						sftpChannel.cd(folder);
					}
				}
			}
			sftpChannel.put(convFile.getAbsolutePath(), tranferPath);
			sftpChannel.exit();
			session.disconnect();
		} catch (JSchException e) {
			logger.error(e.getMessage(), e);
			return false;
		} catch (SftpException ex) {
			logger.error(ex.getMessage(), ex);
			return false;
		}
		return true;
	}

	@Override
	public void uploadFileToWindowsSharedPath(String remotePath, String localPath, String fileName)
			throws FileSystemException {

		String copyPath = remotePath + fileName;

		File localFile = new File(localPath);
		FileSystemManager fileSystemManager = VFS.getManager();

		if (localFile.exists()) {
			// Setup our SFTP configuration
			FileSystemOptions opts = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
			logger.info("remote path : {} ", copyPath);
			logger.info("local path : {}", localFile.getAbsolutePath());
			FileObject localFileObject = fileSystemManager.resolveFile(localPath);
			FileObject copyFileObject = fileSystemManager.resolveFile(copyPath);

			localFileObject.moveTo(copyFileObject);
		}

	}

	@Override
	public void copyFileToAnotherPath(String remotePath, String localPath, String fileName) throws IOException {

		String copyPath = remotePath + fileName;

		logger.info("Source path : {} ", localPath);
		logger.info("Destination path : {}", copyPath);

		File localFile = new File(localPath);
		File destFile = new File(copyPath);

		FileUtils.copyFile(localFile, destFile);

	}
}
