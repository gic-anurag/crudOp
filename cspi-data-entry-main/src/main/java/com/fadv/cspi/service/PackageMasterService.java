package com.fadv.cspi.service;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.PackageMaster;
import com.fadv.cspi.exception.ServiceException;

@Service
public interface PackageMasterService {

	PackageMaster findByPackageMasterId(Long packageMasterId) throws ServiceException;

}
