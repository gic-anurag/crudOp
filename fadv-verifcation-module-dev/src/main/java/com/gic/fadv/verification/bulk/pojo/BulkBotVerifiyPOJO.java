package com.gic.fadv.verification.bulk.pojo;

import java.util.List;

import lombok.Data;

@Data
public class BulkBotVerifiyPOJO {
	
	    private BotMetadataPOJO metadata;

	    private List<BotDataPOJO> data;
}
