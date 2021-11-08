package com.fadv.cspi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.PackageMaster;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.PackageMasterRepository;

@Service
public class PackageMasterServiceImpl implements PackageMasterService {

	private static final String ERROR_CODE_404 = "ERROR_CODE_404";

	@Autowired
	private PackageMasterRepository packageMasterRepository;

	@Override
	public PackageMaster findByPackageMasterId(Long packageMasterId) throws ServiceException {

		Optional<PackageMaster> packageMasterOptional = packageMasterRepository.findById(packageMasterId);
		if (packageMasterOptional.isPresent()) {
			return packageMasterOptional.get();
		}
		throw new ServiceException("Package master not found for given case id", ERROR_CODE_404);
	}
}
