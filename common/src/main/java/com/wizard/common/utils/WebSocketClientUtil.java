package com.wizard.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket客户端工具类
 * 用于订阅WebSocket连接并将接收到的消息存储到Redis中
 *
 * @author wizard
 * @date 2025-01-27
 */
@Slf4j
@Component
public class WebSocketClientUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "websocket:message:";
    private static final String REDIS_KEY_LATEST = "websocket:latest:";
    private static final String REDIS_KEY_STATISTICS = "websocket:stats:";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 创建并启动WebSocket连接
     *
     * @param wsUrl            WebSocket URL
     * @param channelName      频道名称，用于区分不同的数据源
     * @param subscribeMessage 订阅消息，如果为null则不发送订阅消息
     * @return WebSocket客户端实例
     */
    public WebSocketClient createAndStartWebSocket(String wsUrl, String channelName, String subscribeMessage) {
        try {
            URI serverUri = new URI(wsUrl);
            Draft draft = new Draft_6455();

            WebSocketClient client = new WebSocketClient(serverUri, draft) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    WebSocketClientUtil.log.info("WebSocket连接已打开: {} - {}", channelName, wsUrl);

                    // 记录连接状态到Redis
                    String statsKey = REDIS_KEY_STATISTICS + channelName;
                    redisTemplate.opsForHash().put(statsKey, "status", "connected");
                    redisTemplate.opsForHash().put(statsKey, "connectTime",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    redisTemplate.opsForHash().put(statsKey, "url", wsUrl);
                    redisTemplate.expire(statsKey, 24, TimeUnit.HOURS);

                    // 发送订阅消息
                    if (subscribeMessage != null && !subscribeMessage.trim().isEmpty()) {
                        send(subscribeMessage);
                        WebSocketClientUtil.log.info("已发送订阅消息: {}", subscribeMessage);
                    }
                }

                @Override
                public void onMessage(String message) {
                    try {
                        WebSocketClientUtil.log.debug("收到WebSocket消息: {} - {}", channelName, message);

                        // 存储消息到Redis
                        storeMessageToRedis(channelName, message);

                        // 更新统计信息
                        updateStatistics(channelName);

                    } catch (Exception e) {
                        WebSocketClientUtil.log.error("处理WebSocket消息时发生错误: {}", e.getMessage(), e);
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    WebSocketClientUtil.log.warn("WebSocket连接已关闭: {} - 代码: {}, 原因: {}, 远程关闭: {}",
                            channelName, code, reason, remote);

                    // 更新连接状态
                    String statsKey = REDIS_KEY_STATISTICS + channelName;
                    redisTemplate.opsForHash().put(statsKey, "status", "disconnected");
                    redisTemplate.opsForHash().put(statsKey, "disconnectTime",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    redisTemplate.opsForHash().put(statsKey, "closeCode", code);
                    redisTemplate.opsForHash().put(statsKey, "closeReason", reason);
                }

                @Override
                public void onError(Exception ex) {
                    WebSocketClientUtil.log.error("WebSocket连接发生错误: {} - {}", channelName, ex.getMessage(), ex);

                    // 记录错误信息
                    String statsKey = REDIS_KEY_STATISTICS + channelName;
                    redisTemplate.opsForHash().put(statsKey, "status", "error");
                    redisTemplate.opsForHash().put(statsKey, "lastError", ex.getMessage());
                    redisTemplate.opsForHash().put(statsKey, "errorTime",
                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            };

            // 设置连接超时
            client.setConnectionLostTimeout(30);

            // 启动连接
            client.connect();

            // 启动心跳检测
            startHeartbeat(client, channelName);

            return client;

        } catch (Exception e) {
            log.error("创建WebSocket连接失败: {} - {}", channelName, e.getMessage(), e);
            throw new RuntimeException("创建WebSocket连接失败", e);
        }
    }

    /**
     * 存储消息到Redis
     */
    private void storeMessageToRedis(String channelName, String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 1. 存储到时间序列队列（最新1000条）
            String listKey = REDIS_KEY_PREFIX + channelName + ":list";
            redisTemplate.opsForList().leftPush(listKey, message);
            redisTemplate.opsForList().trim(listKey, 0, 999); // 保留最新1000条
            redisTemplate.expire(listKey, 24, TimeUnit.HOURS);

            // 2. 存储最新消息
            String latestKey = REDIS_KEY_LATEST + channelName;
            redisTemplate.opsForValue().set(latestKey, message, 24, TimeUnit.HOURS);

            // 3. 存储到哈希表（按时间戳）
            String hashKey = REDIS_KEY_PREFIX + channelName + ":hash";
            redisTemplate.opsForHash().put(hashKey, timestamp, message);
            redisTemplate.expire(hashKey, 24, TimeUnit.HOURS);

            // 4. 解析JSON并存储结构化数据（如果是JSON格式）
            try {
                Map<String, Object> jsonData = JSON.parseObject(message, Map.class);
                if (jsonData != null) {
                    String structuredKey = REDIS_KEY_PREFIX + channelName + ":structured:" + timestamp;
                    redisTemplate.opsForHash().putAll(structuredKey, jsonData);
                    redisTemplate.expire(structuredKey, 24, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                // 不是JSON格式，忽略
                log.debug("消息不是有效的JSON格式: {}", message);
            }

        } catch (Exception e) {
            log.error("存储消息到Redis失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(String channelName) {
        try {
            String statsKey = REDIS_KEY_STATISTICS + channelName;

            // 增加消息计数
            redisTemplate.opsForHash().increment(statsKey, "messageCount", 1);

            // 更新最后接收消息时间
            redisTemplate.opsForHash().put(statsKey, "lastMessageTime",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            redisTemplate.expire(statsKey, 24, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("更新统计信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 启动心跳检测
     */
    private void startHeartbeat(WebSocketClient client, String channelName) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (client.isOpen()) {
                    // 发送ping消息
                    client.sendPing();
                    WebSocketClientUtil.log.debug("发送心跳ping: {}", channelName);
                } else {
                    WebSocketClientUtil.log.warn("WebSocket连接已断开，尝试重连: {}", channelName);
                    if (client.isClosed() && !client.isClosing()) {
                        client.reconnect();
                    }
                }
            } catch (Exception e) {
                WebSocketClientUtil.log.error("心跳检测失败: {} - {}", channelName, e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 获取频道的最新消息
     */
    public String getLatestMessage(String channelName) {
        try {
            String latestKey = REDIS_KEY_LATEST + channelName;
            Object message = redisTemplate.opsForValue().get(latestKey);
            return message != null ? message.toString() : null;
        } catch (Exception e) {
            log.error("获取最新消息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取频道的历史消息列表
     */
    public java.util.List<Object> getHistoryMessages(String channelName, int count) {
        try {
            String listKey = REDIS_KEY_PREFIX + channelName + ":list";
            return redisTemplate.opsForList().range(listKey, 0, count - 1);
        } catch (Exception e) {
            log.error("获取历史消息失败: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 获取频道的连接统计信息
     */
    public Map<Object, Object> getChannelStatistics(String channelName) {
        try {
            String statsKey = REDIS_KEY_STATISTICS + channelName;
            return redisTemplate.opsForHash().entries(statsKey);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 关闭WebSocket连接
     */
    public void closeWebSocket(WebSocketClient client, String channelName) {
        try {
            if (client != null && client.isOpen()) {
                client.close();
                log.info("WebSocket连接已关闭: {}", channelName);
            }
        } catch (Exception e) {
            log.error("关闭WebSocket连接失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 关闭工具类，清理资源
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
