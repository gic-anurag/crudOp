package com.gic.fadv.verification.online.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.online.model.ManupatraOutput;
import com.gic.fadv.verification.online.pojo.ManupatraOutputPOJO;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class ManupatraOutputController {

	@Autowired
	private com.gic.fadv.verification.online.repository.ManupatraOutputRepository manupatraOutputRepository;

	@GetMapping("/online-title-list/{checkId}")
	public List<ManupatraOutputPOJO> getOnlineTitleSuitByCheckId(@PathVariable(value = "checkId") String checkId) {
		return manupatraOutputRepository.getManupatraOutputByCheckId(checkId);
	}

	@GetMapping("/online-title-suit/{manupatraOutputId}")
	public Optional<ManupatraOutput> getOnlineTitleSuit(
			@PathVariable(value = "manupatraOutputId") Long manupatraOutputId) {
		return manupatraOutputRepository.findById(manupatraOutputId);
	}
}
