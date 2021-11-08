package com.gic.cspi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.gic.cspi.model.Pack_component;
@Repository
public interface Packrepository extends MongoRepository<Pack_component, String>{

}
