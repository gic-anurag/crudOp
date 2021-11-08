package com.gic.fadv.vendor.output.service;

import org.springframework.stereotype.Service;

@Service
public interface VendorOutputServiceNew {

	void processVendorOutputRequest(String serviceName, String fileName, String filePath);

}
