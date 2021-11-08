package com.gic.cspi.service;

import java.util.List;

import com.gic.cspi.model.Pack_component;

public interface PackService {
  public Pack_component savePack_component(Pack_component pc);
  public List<Pack_component> getPack_component();
//  public Pack_component updatePack_component(String id,Pack_component pc);
  public void deletePack_component(String id);
}
