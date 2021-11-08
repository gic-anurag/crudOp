package com.gic.fadv.verification.bulk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gic.fadv.verification.bulk.interfaces.TemplateDetailsInterface;
import com.gic.fadv.verification.bulk.model.BulkTemplateHeaders;

public interface BulkTemplateHeadersRepository extends JpaRepository<BulkTemplateHeaders, Long> {

	@Query(value = "SELECT bulk_template_headers_id AS bulkTemplateHeadersId, template_name AS templateName "
			+ "FROM {h-schema}bulk_template_headers ORDER BY template_name", nativeQuery = true)
	List<TemplateDetailsInterface> getAllTemplateName();

	List<BulkTemplateHeaders> findByTemplateName(String templateName);
	

	List<BulkTemplateHeaders> findByComponentName(String componentName);
	
	@Query(value = "SELECT bulk_template_headers_id AS bulkTemplateHeadersId, template_name AS templateName "
			+ "FROM {h-schema}bulk_template_headers WHERE component_name = :componentName ORDER BY template_name", nativeQuery = true)
	List<TemplateDetailsInterface> getAllTemplateNameByComponent(String componentName);
}
