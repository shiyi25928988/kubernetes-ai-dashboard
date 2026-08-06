package cc.shiyi.base.ai.utils;

import java.util.Random;

public class ConversationIdGenerator {
    private static final Random random = new Random();
    private static final String PREFIX = "conv_";

    public static String generateNewId(Long userId) {
         return "conv_" + userId + "_" + generateEightDigitRandomNumber();
    }

    /**
     * 从对话ID中提取用户ID
     * @param conversationId 对话ID，格式为 conv_用户ID_8位随机数
     * @return 用户ID
     */
    public static Long extractUserId(String conversationId) {
        if (conversationId == null || !conversationId.startsWith(PREFIX)) {
            throw new IllegalArgumentException("无效的对话ID格式");
        }
        String[] parts = conversationId.split("_");
        if (parts.length < 2) {
            throw new IllegalArgumentException("无效的对话ID格式");
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的对话ID格式，无法提取用户ID", e);
        }
    }

    private static String generateEightDigitRandomNumber() {
        int number = random.nextInt(90000000) + 10000000;
        return String.valueOf(number);
    }
}
