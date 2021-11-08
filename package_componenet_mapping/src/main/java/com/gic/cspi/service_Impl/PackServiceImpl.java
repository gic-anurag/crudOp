package com.gic.cspi.service_Impl;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gic.cspi.model.Pack_component;
import com.gic.cspi.repository.Packrepository;
import com.gic.cspi.service.PackService;

@Service
public class PackServiceImpl implements PackService{

	@Autowired
	private Packrepository pr;
	
	@Override
	public Pack_component savePack_component(Pack_component pc) {
		return pr.save(pc);
	}

	@Override
	public List<Pack_component> getPack_component() {
		return pr.findAll();
	}

//	@Override
//	public Pack_component updatePack_component(String id, Pack_component pc) {
//		
//		
//	}

	@Override
	public void deletePack_component(String id) {
		pr.deleteById(id);
		
	}

	
}
