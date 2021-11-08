package com.fadv.cspi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fadv.cspi.entities.PackCompProd;
import com.fadv.cspi.exception.ServiceException;

@Service
public interface PackCompProdService {

	List<PackCompProd> findByPackageMasterId(Long packageMasterId) throws ServiceException;

}
