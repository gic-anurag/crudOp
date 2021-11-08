package com.gic.fadv.spoc.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;

public class ZipUtility {

	@Value("${local.file.download.location}")
	private String localFileDownloadLocation;
	
	@Value("${local.file.zip.location}")
	private String localFileZiplocation;
	/*--------------------------Logic For Zip and Uzip the file----------------------*/

	/*--------------------------Single File Zip*-------------------------------------*/
	public static void singleFileZip() throws IOException {
		String sourceFile = "test1.txt";
		FileOutputStream fos = new FileOutputStream("compressed.zip");
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(sourceFile);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		zipOut.close();
		fis.close();
		fos.close();
	}

	/*--------------Multiple File Zip*------------------------------------*/
	public static void multipleFileZip(List<File> fileList,String path,String checkId) throws IOException {
//		List<String> srcFiles = Arrays.asList("test1.txt", "test2.txt");
		//File fileName= new File(path+"multiCompressed.zip");
		File fileName= new File(path+checkId+".zip");
		FileOutputStream fos = new FileOutputStream(fileName);
		ZipOutputStream zipOut = new ZipOutputStream(fos);
//		for (String srcFile : srcFiles) {
		for (File srcFile : fileList) {
//			File fileToZip = new File(srcFile);
//			FileInputStream fis = new FileInputStream(fileToZip);
//			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
//			zipOut.putNextEntry(zipEntry);
			FileInputStream fis = new FileInputStream(srcFile);
			ZipEntry zipEntry = new ZipEntry(srcFile.getName());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOut.close();
		fos.close();
	}

	/*-----------------Zip Directory------------------------*/

	public static void main() throws IOException {
		String sourceFile = "zipTest";
		FileOutputStream fos = new FileOutputStream("dirCompressed.zip");
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		File fileToZip = new File(sourceFile);

		zipFile(fileToZip, fileToZip.getName(), zipOut);
		zipOut.close();
		fos.close();
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	/*----------------------------- Unzip an Archive---------------------------------*/
	public static void unZipArchive(String localFileDownloadLocation, String fileZip) throws IOException {
		// String fileZip = "src/main/resources/unzipTest/86208.zip";
		System.out.println("Zip File Location" + fileZip);
		// File destDir = new File("src/main/resources/unzipTest");
		System.out.println("localFileDownloadLocation " + localFileDownloadLocation);
		File destDir = new File(localFileDownloadLocation);
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			// ...
			File newFile = newFile(destDir, zipEntry);
			if (zipEntry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				// fix for Windows-created archives
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}

				// write file content
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
