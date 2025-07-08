package com.wizard.common.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wizard
 * @date 2025年07月08日 10:44
 * @desc 全局缓存变量,用于存储所有可交易标的
 */
@Getter
@Slf4j
@Component
public class GlobalListComponent {

	/**
	 * 待分析数据列表
	 * -- GETTER --
	 *  获取默认待分析列表【空List】
	 *
	 * @return List<ResourcePo>

	 */
	private List<String> globalList = new ArrayList<>();

	private static GlobalListComponent instance;

	public static synchronized GlobalListComponent getInstance() {
		if (instance == null) {
			instance = new GlobalListComponent();
		}
		return instance;
	}

	/**
	 * 添加待分析数据
	 * @param logId	日志ID
	 * @param item	数据项
	 */
	public void addToGlobalList(Long logId,String item) {
		globalList.add(item);
		log.info("日志ID:{},添加标的:{}",logId,item);
	}

	public void addToGlobalList(Long logId,List<String> item) {
		globalList.addAll(item);
		log.info("日志ID:{},添加标的:{}",logId,item);
	}

	public void removeAll(Long logId){
		globalList.clear();
		log.info("日志ID:{},删除所有标的:{}",logId);
	}

	/**
	 * 移除待分析数据
	 * @param logId	日志ID
	 * @param item	数据项
	 */
	public void removeFromGlobalList(Long logId,String item) {
		globalList.remove(item);
		log.info("日志ID:{},移除标的:{}",logId,item);
	}
}
