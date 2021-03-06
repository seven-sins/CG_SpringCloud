package com.cg.flink.stream.map;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import com.alibaba.fastjson.JSON;
import com.cg.po.bigdata.Message;
import com.cg.po.bigdata.UserVisit;

/**
 * 消息转换
 * @author Rex.Tan
 * @date 2019年7月30日 下午2:00:17
 */
public class UserVisitMap implements FlatMapFunction<Message, UserVisit> {
	private static final long serialVersionUID = 1L;

	@Override
	public void flatMap(Message value, Collector<UserVisit> out) throws Exception {
		UserVisit userVisit = JSON.parseObject(value.getMessage(), UserVisit.class);
		userVisit.setVisitCount(userVisit.getVisitCount() == null ? 1 : userVisit.getVisitCount());

		out.collect(userVisit);
	}
}
