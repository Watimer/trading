package com.wizard.business.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

	@Autowired
	private StringRedisTemplate redisTemplate;

	// ========================== String ==========================
	public void set(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public void set(String key, String value, long timeoutSeconds) {
		redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
	}

	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public Boolean delete(String key) {
		return redisTemplate.delete(key);
	}

	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	// ========================== Hash ==========================
	public void hSet(String key, String field, String value) {
		redisTemplate.opsForHash().put(key, field, value);
	}

	public void hSetAll(String key, Map<String, String> map) {
		redisTemplate.opsForHash().putAll(key, map);
	}

	public String hGet(String key, String field) {
		Object value = redisTemplate.opsForHash().get(key, field);
		return value != null ? value.toString() : null;
	}

	public Map<Object, Object> hGetAll(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	public void hDelete(String key, String... fields) {
		redisTemplate.opsForHash().delete(key, (Object[]) fields);
	}

	public boolean hHasKey(String key, String field) {
		return redisTemplate.opsForHash().hasKey(key, field);
	}

	// ========================== Key Expire ==========================
	public void expire(String key, long timeoutSeconds) {
		redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS);
	}

	public Long getExpire(String key) {
		return redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	// ========================== List ==========================
	public void lPush(String key, String value) {
		redisTemplate.opsForList().leftPush(key, value);
	}

	public String lPop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	// ========================== Set ==========================
	public void sAdd(String key, String... values) {
		redisTemplate.opsForSet().add(key, values);
	}

	public Set<String> sMembers(String key) {
		return redisTemplate.opsForSet().members(key);
	}

	public void sRemove(String key, String... values) {
		redisTemplate.opsForSet().remove(key, (Object[]) values);
	}

	// ========================== ZSet ==========================
	public void zAdd(String key, String value, double score) {
		redisTemplate.opsForZSet().add(key, value, score);
	}

	public Set<String> zRangeByScore(String key, double min, double max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}

	public void zRemove(String key, String... values) {
		redisTemplate.opsForZSet().remove(key, (Object[]) values);
	}
}
