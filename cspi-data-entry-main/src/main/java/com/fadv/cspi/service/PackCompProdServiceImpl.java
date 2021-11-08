package com.fadv.cspi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.PackCompProd;
import com.fadv.cspi.entities.PackageMaster;
import com.fadv.cspi.exception.ServiceException;
import com.fadv.cspi.repository.PackCompProdRepository;

@Service
public class PackCompProdServiceImpl implements PackCompProdService {

	@Autowired
	private PackCompProdRepository packCompProdRepository;

	@Autowired
	private PackageMasterService packageMasterService;

	@Override
	public List<PackCompProd> findByPackageMasterId(Long packageMasterId) throws ServiceException {
		PackageMaster packageMaster = packageMasterService.findByPackageMasterId(packageMasterId);

		return packCompProdRepository.findByPackageMaster(packageMaster);
	}
}
