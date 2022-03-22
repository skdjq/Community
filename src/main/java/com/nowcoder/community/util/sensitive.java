package com.nowcoder.community.util;

import java.util.HashMap;

public class sensitive {

    private class TrieNode {

        private boolean isEnd = false;

        private HashMap<Character, TrieNode> subNodes = new HashMap<>();

        public void addNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getNodes(Character c) {
            return subNodes.get(c);
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }
    }

}
