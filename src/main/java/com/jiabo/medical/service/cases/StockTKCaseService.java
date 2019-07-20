package com.jiabo.medical.service.cases;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.jiabo.medical.constant.ConstantInfo;
import com.jiabo.medical.entity.DvTimelineDTO;
import com.jiabo.medical.entity.Equipment;
import com.jiabo.medical.entity.PmCaseDTO;
import com.jiabo.medical.entity.StockCaseDTO;
import com.jiabo.medical.entity.StockTKCaseDTO;
import com.jiabo.medical.mapper.DvTimelineMapper;
import com.jiabo.medical.mapper.EquipmentMapper;
import com.jiabo.medical.mapper.MaintainCaseMapper;
import com.jiabo.medical.mapper.StockTKCaseMapper;
import com.jiabo.medical.pojo.ResponseDTO;
import com.jiabo.medical.util.CommonUtils;
import com.jiabo.medical.mapper.UserRoleMapper;


@Service
public class StockTKCaseService {
	
	@Autowired
	private StockTKCaseMapper caseMapper;
	
	@Autowired
	private DvTimelineMapper tlCaseMapper;
	
	@Autowired
	private EquipmentMapper equipMapper;
	
	@Autowired
	private UserRoleMapper userMapper;

	private final Logger log = Logger.getLogger(this.getClass());
	
	public ResponseDTO getAddButtonAuth(StockTKCaseDTO caseDto) {
		
		ResponseDTO res = new ResponseDTO();
		
		List<String> roleNames = userMapper.getRoleName(caseDto.getUserId());
		
		if (roleNames == null || roleNames.size() == 0) {
			res.code = ConstantInfo.INVALID;
			res.message = "该用户未被指派角色!";
			return res;
		} 
		
		boolean canAdd = false;
		
		// 运维主管或科室负责人可以新建盘点
		if (roleNames.get(0).equals(ConstantInfo.ROLE_MANAGER) 
				||  roleNames.get(0).equals(ConstantInfo.ROLE_DEPT_CHARGER) ) {
			canAdd = true;
		}
		
		res.code = ConstantInfo.NORMAL;
		res.data = canAdd;
		
		return res;
		
	}
	
	public ResponseDTO<List<StockTKCaseDTO>> getStockCaseInfos(StockTKCaseDTO caseDto) {

		ResponseDTO<List<StockTKCaseDTO>> res = new ResponseDTO<List<StockTKCaseDTO>>();
		
		List<String> roleNames = userMapper.getRoleName(caseDto.getUserId());
		
		if (roleNames == null || roleNames.size() == 0) {
			res.code = ConstantInfo.INVALID;
			res.message = "该用户未被指派角色!";
			return res;
		}
		
		String roleName = roleNames.get(0);
		
		if (!roleName.equals(ConstantInfo.ROLE_MANAGER)) {
			
			List<Integer> userDeptIds = userMapper.getUserDeptIds(caseDto.getUserId());
			
			if (userDeptIds == null || userDeptIds.size() == 0) {
				res.code = ConstantInfo.INVALID;
				res.message = "该用户未被分配部门!";
				return res;
			} else {
				caseDto.setDepts(userDeptIds);
			}
		}
		
		
		/*boolean engineerFlg = false;
		for (String roleName : roleNames) {
			if (roleName.endsWith(ConstantInfo.ROLE_ENGINEER)) {
				engineerFlg = true;
			}
		}
		
		if (!engineerFlg) {
			caseDto.setAssigneeUserId(null);
		}*/
			
		caseDto.setRoleName(roleName);
		
		int recordsCount = caseMapper.getCaseRecCount(caseDto);
		
		if (recordsCount == 0) {
			res.code = ConstantInfo.INVALID;
			res.message = "没有符合该条件的盘点计划信息!";
			return res;
		}
		
		if (caseDto.getPageIndex() == null) {
			caseDto.setPageIndex(0);
		} else {
			caseDto.setPageIndex(caseDto.getPageIndex()*caseDto.getPageSize());
		}
		
		List<StockTKCaseDTO> mtCases = caseMapper.getStockTKCaseList(caseDto);
		
		res.data = mtCases;
		res.recordCount = recordsCount;
		
		if (res.recordCount == 0) {
			
			res.code = ConstantInfo.INVALID;
			res.message = "没有符合该条件的盘点计划信息!";

		} else {
			res.code = ConstantInfo.NORMAL;
		}
		
		return res;
	}
	
	
	public ResponseDTO<List<StockCaseDTO>> getStockTKDeviceList(StockCaseDTO caseDto) {

		ResponseDTO<List<StockCaseDTO>> res = new ResponseDTO<List<StockCaseDTO>>();
		
		if (caseDto.getPageIndex() == null) {
			
			caseDto.setPageIndex(0);
		} else {
			caseDto.setPageIndex(caseDto.getPageIndex()*10);
		}
		
		List<StockCaseDTO> devList = caseMapper.getStockTKDeviceList(caseDto);
		
		res.data = devList;
		res.recordCount = devList.size();
		res.code = ConstantInfo.NORMAL;
		
		return res;
	}


@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=36000,rollbackFor=Exception.class)
public ResponseDTO completePmCase(PmCaseDTO caseDto) {

	ResponseDTO res = new ResponseDTO();
	
	if (caseDto.getDeviceId() == null) {
		res.code = ConstantInfo.INVALID;
		res.message = "未选择巡检设备!";
		return res;
	}

	if (caseDto.getActualUserId() == null) {
		res.code = ConstantInfo.INVALID;
		res.message = "未输入实际保养人!";
		return res;
	}

	// 工单状态设置为'已关闭（50）'
	caseDto.setCaseState(50);
	
	Timestamp now = new Timestamp(System.currentTimeMillis());
	
	caseDto.setModifyTime(now);
	caseDto.setCreateTime(now);
	
	try {
		int count=0;
		//= caseMapper.updStockTKCase(caseDto);
		
		if (count > 0) {
			
			Equipment intervalInfo = equipMapper.getPmDeviceInfo(caseDto.getDeviceId());
			
			Equipment equip = new Equipment();
			
			equip.setDeviceId(caseDto.getDeviceId());
			
			long interval = intervalInfo.getMaintenanceInterval();
			
			long nextPmDate = now.getTime() + interval*24*3600*1000;
			
			equip.setNextMaintenanceDate(new Timestamp(nextPmDate));
			
			equipMapper.updEquipPmTime(equip);
			
			res.code = ConstantInfo.NORMAL;
			res.message = "工单更新成功";
			res.recordCount = count;
			res.data = caseDto;
			
		} else {
			res.code = ConstantInfo.INVALID;
			res.message = "工单更新失败，请确认是否存在此纪录";
		}
	} catch (DataAccessException e) {
		res.code = ConstantInfo.INVALID;
		res.message = e.getMessage();
		log.error(e.getMessage());
	}
	
	return res;
	
}

	public ResponseDTO rotateTKCaseState(Integer assigneeUserId) {
		ResponseDTO res = new ResponseDTO();
		
		if (assigneeUserId == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "被指派人ID为空";
			
			return res;
		}
		
		//轮询待处理工单
		int count = caseMapper.rotateTKCaseState(assigneeUserId);
		
		if (count > 0) {
			res.code = ConstantInfo.NORMAL;
			res.recordCount = count;
			res.message = "有新的盘点工单待处理";
		} else {
			res.code = ConstantInfo.NORMAL;
			res.message = "";
		}
		
		return res;
	}

	// 增加盘点单
	@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=36000,rollbackFor=Exception.class)
	public  ResponseDTO addStockTKCase(StockTKCaseDTO caseDto) {
		
		
		ResponseDTO res = new ResponseDTO();
		
		if (caseDto.getHospitalId() == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "未指定医院";
			
			return res;
		}
		
		if (caseDto.getDepts() == null) {
			
			if (caseDto.getDeptId() == null) {
				res.code = ConstantInfo.INVALID;
				res.message = "未指定科室";
				
				return res;
			} else {
				List<Integer> depts = new ArrayList<Integer>();
				depts.add(caseDto.getDeptId());
				caseDto.setDepts(depts);
			}
			
		}
		
		if (caseDto.getAssigneeUserId() == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "未指定盘点人";
			
			return res;
		}
		
		if (StringUtils.isEmpty(caseDto.getPlanBeginTime())) {
			res.code = ConstantInfo.INVALID;
			res.message = "未输入计划盘点开始时间!";
			return res;
		}
		
		if (StringUtils.isEmpty(caseDto.getPlanEndTime())) {
			res.code = ConstantInfo.INVALID;
			res.message = "未输入计划盘点结束时间!";
			return res;
		}
		
		// 工单状态设置为'待执行（10）'
		caseDto.setCaseState(10);
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		caseDto.setModifyTime(now);
		caseDto.setCreateTime(now);
		
		int count = caseMapper.addStockTKCase(caseDto);
		
		if (count > 0) {
			int caseSeqNo= caseMapper.getSequenceNo();
			
			StockCaseDTO stkDto = new StockCaseDTO();
			stkDto.setCaseId(caseSeqNo);
			
			for (Integer deptId : caseDto.getDepts()) {
				stkDto.setDeptId(deptId);
				
				
				
				List<Integer> deviceIds = caseMapper.getDeviceIds(deptId);
				
				if (deviceIds == null || deviceIds.size() == 0) {
					res.code = ConstantInfo.INVALID;
					res.message = "该部门不存在待盘点仪器!";
					return res;
				}
				
				count = caseMapper.addStockDeptCase(stkDto);
				
				for (Integer deviceId : deviceIds) {
					stkDto.setDeviceId(deviceId);
					count = caseMapper.addStockDeviceCase(stkDto);
					
				}
			}
			
			if (count > 0) {
				res.code = ConstantInfo.NORMAL;
				res.message = "盘点单创建成功";
			}
			
		}
		
		return res;
		
	}



	public ResponseDTO updStockTKCase(StockCaseDTO caseDto) {
		
		ResponseDTO res = new ResponseDTO();
		
		if (caseDto.getOperationUserId() == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "未指定实际盘点人";
			
			return res;
		}
		
		if (StringUtils.isEmpty(caseDto.getOperationTime())) {
			res.code = ConstantInfo.INVALID;
			res.message = "未输入实际盘点时间!";
			return res;
		}
		
		Integer recentCaseId = caseMapper.getRecentCaseId(caseDto.getDeviceId());
		caseDto.setCaseId(recentCaseId);
	
		
		if (recentCaseId == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "该设备对应盘点单不存在!";
			return res;
		}
		
		int count = caseMapper.updStockTKDevCase(caseDto);
		
		if (count > 0) {
			res.code = ConstantInfo.NORMAL;
			res.message = "该设备盘点成功";
			
			StockTKCaseDTO tkCaseDto = new StockTKCaseDTO();
			tkCaseDto.setCaseId(caseDto.getCaseId());
			
			count = caseMapper.getDeviceNotTKCount(caseDto.getCaseId());
			// 该计划所有设备都盘点完毕
			if (count == 0) {
				
				Timestamp now = new Timestamp(System.currentTimeMillis());
				
				tkCaseDto.setCaseState(50);
				tkCaseDto.setActualTime(CommonUtils.getTimeString(now));
				caseMapper.updStockTKCase(tkCaseDto);
				
				
				DvTimelineDTO timelineDto = new DvTimelineDTO();
				
				timelineDto.setDeviceId(caseDto.getDeviceId());
				timelineDto.setEventSubject(caseDto.getCaseSubject());
				timelineDto.setEventType(20);  // 报修
				timelineDto.setEventId(caseDto.getCaseId());
				timelineDto.setEventTime(now);
				
				timelineDto.setCreateTime(now);
				timelineDto.setModifyTime(now);
				timelineDto.setCreater(caseDto.getOperationUserId());
				timelineDto.setModifier(caseDto.getOperationUserId());
				timelineDto.setUserId(caseDto.getOperationUserId());
				
				tlCaseMapper.addDvTimelineCase(timelineDto);

			} else {
				
				count = caseMapper.getDeviceTKCount(caseDto.getCaseId());
				
				// 开始设备盘点
				if (count == 1) {
					tkCaseDto.setCaseState(30);
					caseMapper.updStockTKCase(tkCaseDto);
				}
			}
		}
		return res;
	}
	
	// 审核盘点计划
	public ResponseDTO updStockTKCaseState(StockCaseDTO caseDto) {
		
		ResponseDTO res = new ResponseDTO();
		
		if (caseDto.getModifier() == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "未指定审核人";
			
			return res;
		}
		
		// 无caseId场合
		if (caseDto.getCaseId() == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "请指定一个盘点计划!";
			return res;
		}
		
		// 更新审核状态
		caseDto.setAuditState(1);
		//状态：已审核
		caseDto.setCaseState(60);
		
		/*Timestamp now = new Timestamp(System.currentTimeMillis());
		
		caseDto.setModifyTime(now);*/
		
		int count = caseMapper.updStockTKCaseState(caseDto);
		
		if (count > 0) {
			res.code = ConstantInfo.NORMAL;
			res.message = "盘点计划审核成功";
			
			
		}
		return res;
	}
	
	// 审核盘点计划
	public ResponseDTO delStockTKCase(StockCaseDTO caseDto) {
			
			ResponseDTO res = new ResponseDTO();
			
			// 无caseId场合
			if (caseDto.getCaseId() == null) {
				res.code = ConstantInfo.INVALID;
				res.message = "请指定一个盘点计划!";
				return res;
			}
			
			int count = caseMapper.delStockTKCase(caseDto.getCaseId());
			
			if (count > 0) {
				res.code = ConstantInfo.NORMAL;
				res.message = "盘点计划删除成功";
				
				
			}
			return res;
		}


	public ResponseDTO getStockTKDevice(StockCaseDTO caseDto) {
		ResponseDTO<StockTKCaseDTO> res = new ResponseDTO<StockTKCaseDTO>();

		StockTKCaseDTO mtCase = caseMapper.getStockTKDevice(caseDto);
		
		if (mtCase == null) {
			res.code = ConstantInfo.INVALID;
			res.message = "没有符合该条件的盘点设备信息!";
			return res;
		}
		
		res.data = mtCase;
		res.recordCount = 1;
		res.code = ConstantInfo.NORMAL;
		
		return res;
	}
	
	

}
