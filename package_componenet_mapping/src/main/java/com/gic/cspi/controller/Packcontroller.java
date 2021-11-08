package com.gic.cspi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.cspi.model.Pack_component;
import com.gic.cspi.service.PackService;

@RestController
@RequestMapping("/pack")
public class Packcontroller {
 
	@Autowired
	private PackService ps;
	
//	
//	@PostMapping("/save")
//	public Pack_component savePackComponent(@RequestBody Pack_component pc) {
//		return  ps.savePack_component(pc); 
//	}
	
	@GetMapping("/list")
	public List<Pack_component> getPack_component(@RequestBody Pack_component pc){
		return ps.getPack_component();
}
//	@PutMapping("/update/{pc_id}")
//	public Pack_component updatePack_component(@RequestBody Pack_component pc, @PathVariable ("user_id") String id) {
//		return ps.updatePack_component(id,pc);
//	}
	
//	@PutMapping("/delete/{pc-id}")
//	public String deletePack_component(@PathVariable("pc_id") String id) {
//		ps.deletePack_component(id);
//		return "package deleted sucessfully";
//	}
	
	
	
	
	
}

	
	
