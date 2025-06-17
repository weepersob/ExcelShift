package com.excel.shift;


import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 油气显示结论表(MUD_GEO_OILGAS_SHOW)
 * </p>
 *
 * @author libiao
 * @since 2025-06-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
//@TableName("MUD_GEO_OILGAS_SHOW")
//@ApiModel(value="MudGeoOilgasShow对象", description="油气显示结论表(MUD_GEO_OILGAS_SHOW)")
public class MudGeoOilgasShow implements Serializable {

    private static final long serialVersionUID = 1L;

//    @ApiModelProperty(value = "油气综合显示")
//    @TableId(value = "OILGAS_SHOWID", type = IdType.ASSIGN_ID)
    private String oilgasShowid;

//    @ApiModelProperty(value = "地质摘要ID")
    private String geologyId;

//    @ApiModelProperty(value = "井ID")
    private String wellId;

//    @ApiModelProperty(value = "井筒ID")
    private String wellboreId;

//    @ApiModelProperty(value = "作业阶段ID")
    private String eventId;

//    @ApiModelProperty(value = "顶深(m)")
    private Double topDepth;

//    @ApiModelProperty(value = "底深(m)")
    private Double botDepth;

//    @ApiModelProperty(value = "厚度(m)")
    private Double invateDepth;

//    @ApiModelProperty(value = "顶垂深(m)")
    private Double topTvd;

//    @ApiModelProperty(value = "底垂深(m)")
    private Double botTvd;

//    @ApiModelProperty(value = "垂深厚度(m)")
    private Double invateTvd;

//    @ApiModelProperty(value = "层位")
    private String strataId;

//    @ApiModelProperty(value = "岩性")
    private String lithId;

//    @ApiModelProperty(value = "颜色代码")
    private String color;

//    @ApiModelProperty(value = "钻时(min/m)范围开始值")
    private Double ropStart;

//    @ApiModelProperty(value = "钻时(min/m)范围结束值")
    private Double ropEnd;

//    @ApiModelProperty(value = "气测全量(%)范围开始值")
    private Double tgStart;

//    @ApiModelProperty(value = "甲烷(%)范围开始值")
    private Double c1Start;

//    @ApiModelProperty(value = "乙烷(%)范围开始值")
    private Double c2Start;

//    @ApiModelProperty(value = "丙烷(%)范围开始值")
    private Double c3Start;

//    @ApiModelProperty(value = "异丁烷(%)范围开始值")
    private Double ic4Start;

//    @ApiModelProperty(value = "正丁烷(%)范围开始值")
    private Double nc4Start;

//    @ApiModelProperty(value = "异戊烷(%)范围开始值")
    private Double ic5Start;

//    @ApiModelProperty(value = "正戊烷(%)范围开始值")
    private Double nc5Start;

//    @ApiModelProperty(value = "二氧化碳(%)范围开始值")
    private Double co2Start;

//    @ApiModelProperty(value = "其它非烃(%)范围开始值")
    private Double inorganicStart;

//    @ApiModelProperty(value = "气测全量(%)范围开始值2")
    private Double tgEnd;
//
//    @ApiModelProperty(value = "甲烷(%)范围结束值")
    private Double c1End;

//    @ApiModelProperty(value = "乙烷(%)范围结束值")
    private Double c2End;

//    @ApiModelProperty(value = "丙烷(%)范围结束值")
    private Double c3End;

//    @ApiModelProperty(value = "异丁烷(%)范围结束值")
    private Double ic4End;

//    @ApiModelProperty(value = "正丁烷(%)范围结束值")
    private Double nc4End;

//    @ApiModelProperty(value = "异戊烷(%)范围结束值")
    private Double ic5End;

//    @ApiModelProperty(value = "正戊烷(%)范围结束值")
    private Double nc5End;

//    @ApiModelProperty(value = "二氧化碳(%)范围结束值")
    private Double co2End;

//    @ApiModelProperty(value = "其它非烃(%)范围结束值")
    private Double inorganicEnd;

//    @ApiModelProperty(value = "荧光面积")
    private Double fluoArea;

//    @ApiModelProperty(value = "荧光级别")
    private String fluoGrade;

//    @ApiModelProperty(value = "直照颜色")
    private String wetColorId;
//
//    @ApiModelProperty(value = "滴照颜色")
    private String cutColorId;

//    @ApiModelProperty(value = "滴照反应")
    private String reactionId;

//    @ApiModelProperty(value = "槽面显示")
    private String ditchShow;

//    @ApiModelProperty(value = "钻井液密度(g/cm3)")
    private Double density;

//    @ApiModelProperty(value = "钻井液粘度(s/qt)")
    private Double fv;

//    @ApiModelProperty(value = "现场解释")
    private String conclution;

//    @ApiModelProperty(value = "备注")
    private String note2;

//    @ApiModelProperty(value = "0:正常,1:删除")
//    @TableLogic(value = "0", delval = "1")
//    @TableField(value = "DELETE_MARK", fill = FieldFill.INSERT)
    private Integer deleteMark;

//    @ApiModelProperty(value = "创建人")
//    @TableField(value = "CREATE_USER_ID", fill = FieldFill.INSERT)
    private String createUserId;

//    @ApiModelProperty(value = "创建时间")
//    @TableField(value = "CREATE_DATE", fill = FieldFill.INSERT)
//    @ConvertTime
    private LocalDateTime createDate;

//    @ApiModelProperty(value = "修改人")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String modityUserId;

//    @ApiModelProperty(value = "修改时间")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
//    @ConvertTime
    private LocalDateTime modityDate;


}
