package com.wizard.common.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wizard
 * @date 2024-09-19
 * @desc 接口响应结构体
 */
@Data
public class ResultInfo<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 *	状态码
	 */
	private int code;

	/**
	 * 响应信息
	 */
	private String msg;

	/**
	 * 响应数据
	 */
	private T data;
}
