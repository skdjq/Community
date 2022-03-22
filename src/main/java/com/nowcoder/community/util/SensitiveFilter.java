package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }


    @PostConstruct
    public void init()  {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode tmp = rootNode;
        for(int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tmp.getSubNode(c);
            if(subNode == null) {
                subNode = new TrieNode();
                tmp.addSubNode(c, subNode);
            }
            tmp = subNode;
            if(i == keyword.length() - 1) {
                tmp.setKeywordEnd(true);
            }
        }
    }


    /**
     * 返回过滤后的文本
     * */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode tmp = rootNode;
        int begin = 0;
        int position = 0;

        StringBuilder sb = new StringBuilder();
        while(position < text.length()) {
            char c = text.charAt(position);
            // 忽略特殊符号
            if(isSymbol(c)) {
                if(tmp == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tmp = tmp.getSubNode(c);
            if(tmp == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                begin++;
                position = begin;
                tmp = rootNode;
            } else if (tmp.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                position++;
                begin = position;
            } else {
                position++;
            }
        }
        // 将剩下的字符串加入结果中
        sb.append(text.substring(begin));

        return sb.toString();
    }


    // 判断是否为符号
    // 0x2E80 ~ 0x9FFF 是东亚文字范围
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}
