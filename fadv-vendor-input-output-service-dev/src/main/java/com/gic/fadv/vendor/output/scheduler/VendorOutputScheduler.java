package com.gic.fadv.vendor.output.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gic.fadv.vendor.service.VendorSftpConnectionService;

@Component
public class VendorOutputScheduler {

	private static final String PATH_DELIMITER = "/";

	@Autowired
	private VendorSftpConnectionService vendorSftpConnectionService;

	@Value("${server.sftp.output.file.path}")
	private String outputSftpPath;

	private static final Logger logger = LoggerFactory.getLogger(VendorOutputScheduler.class);

	@Scheduled(cron = "${scheduled.file.watcher.cron}")
	public void runParallel() {
		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				vendorOutputAddress();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		});

		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				vendorOutputEducation();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		});

//		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
//			try {
//				vendorOutputCriminal();
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//				e.printStackTrace();
//			}
//		});

		CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2);
		try {
			future.get(); // this line waits for all to be completed
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Interrupted! : {}", e.getMessage());
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
	}

	public void vendorOutputAddress() {

		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		int strMonth = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("dd");
		int strDay = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("yyyy");
		int strYear = Integer.parseInt(formatter.format(date));

		String path = outputSftpPath + "Address/" + strYear + PATH_DELIMITER + strMonth + PATH_DELIMITER + strDay;
		if (!vendorSftpConnectionService.checkValidPath(path)) {
			vendorSftpConnectionService.createSftpDirectory(path);
		}
		vendorSftpConnectionService.sfptAddressListener(path, strDay);
	}

	public void vendorOutputEducation() {

		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		int strMonth = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("dd");
		int strDay = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("yyyy");
		int strYear = Integer.parseInt(formatter.format(date));

		String path = outputSftpPath + "Education/" + strYear + PATH_DELIMITER + strMonth + PATH_DELIMITER + strDay;
		if (!vendorSftpConnectionService.checkValidPath(path)) {
			vendorSftpConnectionService.createSftpDirectory(path);
		}
		vendorSftpConnectionService.sfptEducationListener(path, strDay);
	}

	public void vendorOutputCriminal() {

		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		int strMonth = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("dd");
		int strDay = Integer.parseInt(formatter.format(date));
		formatter = new SimpleDateFormat("yyyy");
		int strYear = Integer.parseInt(formatter.format(date));

		String path = "/optexpva/va_fs/FinalReport/Criminal/" + strYear + PATH_DELIMITER + strMonth + PATH_DELIMITER
				+ strDay;
		if (!vendorSftpConnectionService.checkValidPath(path)) {
			vendorSftpConnectionService.createSftpDirectory(path);
		}
		vendorSftpConnectionService.sfptCriminalListener(path, strDay);
	}
}
