package com.excel.shift;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 油气综合显示(MUD_GEO_OILGAS)
 * </p>
 *
 * @author libiao
 * @since 2025-06-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)

public class MudGeoOilgas implements Serializable {

    private static final long serialVersionUID = 1L;

//    @ApiModelProperty(value = "油气综合显示")
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

//    @ApiModelProperty(value = "层位")
    private String strataId;

//    @ApiModelProperty(value = "岩性")
    private String lithId;

//    @ApiModelProperty(value = "钻时(min/m)")
    private Double rop;

//    @ApiModelProperty(value = "含油产状")
    private String showGradeId;

//    @ApiModelProperty(value = "岩屑荧光级别")
    private String logFluoGrade;

//    @ApiModelProperty(value = "壁心荧光级别")
    private String swFluoGrade;

//    @ApiModelProperty(value = "岩心荧光级别")
    private String coreFluoGrade;

//    @ApiModelProperty(value = "槽面显示")
    private String ditchShow;

//    @ApiModelProperty(value = "气测全量(%)")
    private Double tg;

//    @ApiModelProperty(value = "甲烷(%)")
    private Double c1;

//    @ApiModelProperty(value = "乙烷(%)")
    private Double c2;

//    @ApiModelProperty(value = "丙烷(%)")
    private Double c3;

//    @ApiModelProperty(value = "异丁烷(%)")
    private Double ic4;

//    @ApiModelProperty(value = "正丁烷(%)")
    private Double nc4;

//    @ApiModelProperty(value = "异戊烷(%)")
    private Double ic5;

//    @ApiModelProperty(value = "正戊烷(%)")
    private Double nc5;

//    @ApiModelProperty(value = "二氧化碳(%)")
    private Double co2;

//    @ApiModelProperty(value = "其它非烃(%)")
    private Double inorganic;

//    @ApiModelProperty(value = "钻井液密度(g/cm3)")
    private Double density;

//    @ApiModelProperty(value = "钻井液粘度(s/qt)")
    private Double fv;

//    @ApiModelProperty(value = "Qft")
    private Double qft;

//    @ApiModelProperty(value = "解释结论")
    private String conclutionId;

//    @ApiModelProperty(value = "备注")
    private String note2;

//    @ApiModelProperty(value = "0:正常,1:删除")
    private Integer deleteMark;

//    @ApiModelProperty(value = "创建人")
    private String createUserId;

//    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createDate;

//    @ApiModelProperty(value = "修改人")
    private String modityUserId;

//    @ApiModelProperty(value = "修改时间")
    private LocalDateTime modityDate;


}
