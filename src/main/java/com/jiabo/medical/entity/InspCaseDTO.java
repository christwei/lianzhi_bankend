package com.jiabo.medical.entity;

import java.io.Serializable;
import java.sql.Timestamp;


public class InspCaseDTO implements Serializable {
	
	private Integer pageIndex = null;
	private Integer pageSize = 10;
	private Integer caseId= null;
	private String caseSubject;
	private String caseRemark;
	private Integer caseState=null;
	private Integer assigneeUserId=null;
	private Integer deviceId=null;
	private Integer inspectionType=null;
	private Integer deviceOnState;
	private Integer deviceElecEvnState;
	private Integer deviceFuncState;
	private String deviceParamInput;
	private String hospital;
	private String deviceCode;
	private String deviceName;
	private String deptName;
	private String assigneeUserName;
	private String inspectionTime= null;
	private String inspectionRemark= null;

	private Timestamp createTime;
	private Integer creater;
	private Timestamp modifyTime;
	private Integer modifier;
	
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getAssigneeUserName() {
		return assigneeUserName;
	}
	public void setAssigneeUserName(String assigneeUserName) {
		this.assigneeUserName = assigneeUserName;
	}
	
	public Integer getCaseId() {
		return caseId;
	}
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}

	public String getCaseSubject() {
		return caseSubject;
	}
	public void setCaseSubject(String caseSubject) {
		this.caseSubject = caseSubject;
	}
	public String getCaseRemark() {
		return caseRemark;
	}
	public void setCaseRemark(String caseRemark) {
		this.caseRemark = caseRemark;
	}
	public Integer getCaseState() {
		return caseState;
	}
	public void setCaseState(Integer caseState) {
		this.caseState = caseState;
	}
	public Integer getAssigneeUserId() {
		return assigneeUserId;
	}
	public void setAssigneeUserId(Integer assigneeUserId) {
		this.assigneeUserId = assigneeUserId;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Integer getCreater() {
		return creater;
	}
	public void setCreater(Integer creater) {
		this.creater = creater;
	}
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Integer getModifier() {
		return modifier;
	}
	public void setModifier(Integer modifier) {
		this.modifier = modifier;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	public String getHospital() {
		return hospital;
	}
	public void setHospital(String hospital) {
		this.hospital = hospital;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	
	public Integer getInspectionType() {
		return inspectionType;
	}
	public void setInspectionType(Integer inspectionType) {
		this.inspectionType = inspectionType;
	}

	public Integer getDeviceOnState() {
		return deviceOnState;
	}
	public void setDeviceOnState(Integer deviceOnState) {
		this.deviceOnState = deviceOnState;
	}
	public Integer getDeviceElecEvnState() {
		return deviceElecEvnState;
	}
	public void setDeviceElecEvnState(Integer deviceElecEvnState) {
		this.deviceElecEvnState = deviceElecEvnState;
	}
	public Integer getDeviceFuncState() {
		return deviceFuncState;
	}
	public void setDeviceFuncState(Integer deviceFuncState) {
		this.deviceFuncState = deviceFuncState;
	}
	public String getDeviceParamInput() {
		return deviceParamInput;
	}
	public void setDeviceParamInput(String deviceParamInput) {
		this.deviceParamInput = deviceParamInput;
	}
	public String getInspectionRemark() {
		return inspectionRemark;
	}
	public void setInspectionRemark(String inspectionRemark) {
		this.inspectionRemark = inspectionRemark;
	}
	public String getInspectionTime() {
		return inspectionTime;
	}
	public void setInspectionTime(String inspectionTime) {
		this.inspectionTime = inspectionTime;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}
	
}
