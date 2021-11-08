package com.gic.fadv.online.pv.pojo;
import lombok.Data;
@Data
public class PVRequest
{
    private Metadata metadata;

    private PVData[] data;

}
