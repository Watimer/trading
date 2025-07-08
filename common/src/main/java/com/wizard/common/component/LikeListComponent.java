package com.wizard.common.component;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.wizard.common.enums.IntervalEnum;
import com.wizard.common.model.MarketQuotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author wizard
 * @date 2025年07月08日 13:12
 * @desc 自选列表,缓存最近的行情数据
 */
@Slf4j
@Component
public class LikeListComponent {

	/**
	 * 自选列表
	 * 数据结构为Map<symbol,Map<时间级别,行情数据>>
	 */
	private Map<String,Map<IntervalEnum,List<MarketQuotation>>> optionalMap = new HashMap<>();

	private static LikeListComponent instance;

	public static synchronized LikeListComponent getInstance() {
		if (instance == null) {
			instance = new LikeListComponent();
		}
		return instance;
	}

	/**
	 * 添加待分析数据
	 * @param logId		日志ID
	 * @param item		数据项
	 * @param mapList	多周期行情数据
	 */
	public void addOptional(Long logId,String item,Map<IntervalEnum,List<MarketQuotation>> mapList) {
		if(optionalMap.containsKey(item)){
			log.info("日志ID:{},标的:{},已在自选列表,无需额外添加",logId,item);
			return;
		}
		optionalMap.put(item,mapList);
		log.info("日志ID:{},成功添加标的:{}",logId,item);
	}

	/**
	 * 添加最新的行情数据
	 * @param logId					日志ID
	 * @param item					标的名称
	 * @param intervalEnum			时间周期
	 * @param marketQuotation		最新行情信息
	 */
	public void addOptionalMarketQuotation(Long logId,String item,IntervalEnum intervalEnum,MarketQuotation marketQuotation) {
		if(!optionalMap.containsKey(item)){
			log.info("日志ID:{},标的:{},未在自选列表",logId,item);
			return;
		}
		Map<IntervalEnum, List<MarketQuotation>> intervalEnumListMap = optionalMap.get(item);
		if(ObjectUtil.isNull(intervalEnumListMap)){
			intervalEnumListMap = new HashMap<>();
			intervalEnumListMap.put(intervalEnum,new ArrayList<>());
		}
		List<MarketQuotation> marketQuotationList = intervalEnumListMap.get(intervalEnum);
		if(CollUtil.isEmpty(marketQuotationList)){
			marketQuotationList = new ArrayList<>();

		}
		marketQuotationList.add(marketQuotation);
		int size = 500;
		// 只保留最近500条数据
		if(marketQuotationList.size() > size){
			int removeSize = marketQuotationList.size() - size;
			while (removeSize > 0) {
				marketQuotationList.remove(0);
				removeSize--;
			}
		}
		// 数据放入缓存
		intervalEnumListMap.put(intervalEnum, marketQuotationList);
		optionalMap.put(item,intervalEnumListMap);
		log.info("日志ID:{},标的:{},周期:{},数据填充完成",logId,item,intervalEnum.getCode());
	}

	/**
	 * 查询指定级别的数据
	 * @param logId				日志ID
	 * @param item				标的
	 * @param intervalEnum		时间级别
	 * @return
	 */
	public List<MarketQuotation> getOptionalMarketQuotation(Long logId,String item,IntervalEnum intervalEnum) {
		if(optionalMap.containsKey(item)){
			log.info("日志ID:{},标的:{},不在自选列表,需额外添加",logId,item);
			return null;
		}
		Map<IntervalEnum, List<MarketQuotation>> intervalEnumListMap = optionalMap.get(item);
		if(intervalEnumListMap.containsKey(intervalEnum)){
			log.info("日志ID:{},标的:{},周期:{},不在自选列表,需额外添加",logId,item,intervalEnum.getCode());
			return null;
		}
		return intervalEnumListMap.get(intervalEnum);
	}

	/**
	 * 获取指定标的的所有周期行情数据
	 * @param logId				日志ID
	 * @param item				标的
	 * @return
	 */
	public Map<IntervalEnum, List<MarketQuotation>> getOptionalMap(Long logId,String item) {
		if(optionalMap.containsKey(item)){
			log.info("日志ID:{},标的:{},不在自选列表,需额外添加",logId,item);
			return null;
		}
		return optionalMap.get(item);
	}
}
