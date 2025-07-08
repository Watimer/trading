package com.wizard.business.controller;

import com.wizard.common.base.ResultInfo;
import com.wizard.common.component.GlobalListComponent;
import com.wizard.common.utils.ResultInfoUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月08日 10:50
 * @desc
 */
@Slf4j
@RestController
public class StartController {

	@Resource
	HttpServletRequest request;

	@Resource
	GlobalListComponent globalListComponent;

	@GetMapping("/getList")
	public ResultInfo<List<String>> getList(){
		List<String> list = globalListComponent.getGlobalList();

		return ResultInfoUtil.buildSuccess(null,list);
	}
}
