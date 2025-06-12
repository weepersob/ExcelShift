package com.excel.shift;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ComplexEventsTemplateVO {
//    @ApiModelProperty(value = "井名")
    private String wellName;

//    @ApiModelProperty(value = "复杂事故类型")
    private String complexEventType;

//    @ApiModelProperty(value = "复杂事故名称")
    private String complexEventName;

//    @ApiModelProperty(value = "发生事故顶深")
    private Double occurrenceEventTopDepth ;

//    @ApiModelProperty(value = "发生事故时间")
    private LocalDateTime occurrenceEventTime ;

//    @ApiModelProperty(value = "发生事故底深")
    private Double occurrenceEventEndDepth ;

//    @ApiModelProperty(value = "解决事故时间")
    private LocalDate solutionEventTime ;

//    @ApiModelProperty(value = "事故描述")
    private String complexEventDesc;

//    @ApiModelProperty(value = "事故原因")
    private String complexEventReason;

//    @ApiModelProperty(value = "事故处理情况")
    private String complexEventProcess;

//    @ApiModelProperty(value = "泥浆类型")
    private String mudType;

//    @ApiModelProperty(value = "泥浆密度（g/cm3）")
    private Double mudDen;

//    @ApiModelProperty(value = "漏失量(L)")
    private Double leakageAmount;

//    @ApiModelProperty(value = "仪器串组合")
    private String tool;

//    @ApiModelProperty(value = "仪器串长度（m）")
    private Double toolLength;

//    @ApiModelProperty(value = "仪器串重量（lbs）")
    private Double toolWeight;

//    @ApiModelProperty(value = "仪器串最大外径（in）")
    private Double toolMaxOuterDiameter;

//    @ApiModelProperty(value = "仪器落井长度（m）")
    private Double toolFallenLength;
}
