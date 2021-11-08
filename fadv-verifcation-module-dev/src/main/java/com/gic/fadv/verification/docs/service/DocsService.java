package com.gic.fadv.verification.docs.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.docs.pojo.AssociateDocs;
import com.gic.fadv.verification.docs.pojo.CheckIds;
import com.gic.fadv.verification.docs.pojo.DocsData;
import com.gic.fadv.verification.docs.pojo.Response;
import com.gic.fadv.verification.docs.pojo.ResponseAssociateDocs;

@Service
public class DocsService {

	private static final Logger logger = LoggerFactory.getLogger(DocsService.class);

	public List<String> getAssociateDocsUrlData(ResponseAssociateDocs responseAssociateDocs, String checkId) {

		String filePathRes = "";
		List<String>filePathResList=new ArrayList<>();
		
		try {
			Response response = null;
			if (responseAssociateDocs != null && responseAssociateDocs.getSuccess()) {
				response = responseAssociateDocs.getResponse();

				if (response != null) {

					if (response.getAssociateDocs() != null && !response.getAssociateDocs().isEmpty()) {

						for (AssociateDocs associateDocs : response.getAssociateDocs()) {

							if (associateDocs.getDocsData() != null && !associateDocs.getDocsData().isEmpty()) {

								for (DocsData assDocs : associateDocs.getDocsData()) {

									if (assDocs.getCheckIds() != null && !assDocs.getCheckIds().isEmpty()) {
										for (CheckIds checkIds : assDocs.getCheckIds()) {

											if (checkId.equals(checkIds.getCheckId())) {
												filePathResList.add(assDocs.getFilePath());
												
											}
											
										   }
									}
									

								}

							} else {
								filePathRes = "Associated docs not found";
								filePathResList.add(filePathRes);
							}

						}
					}
				} else {
					filePathRes = "Response not found";
					filePathResList.add(filePathRes);
				}
			} else {
				filePathRes = "Data not found";
				filePathResList.add(filePathRes);
			}

			return filePathResList;

		} catch (Exception ex) {

			filePathRes = "Exception Occured while getting associateDocsUrl";
			filePathResList.add(filePathRes);
			logger.error("Exception Occured while getting associateDocsUrl", ex);
		}

		return filePathResList;
	}

}
