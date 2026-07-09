package com.smartagent.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartagent.tool.Tool;
import com.smartagent.tool.ToolDefinition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Web search tool - searches the internet using Baidu search.
 * Accessible from China without VPN/proxy issues.
 */
@Component
public class WebSearchTool implements Tool {

    @Override
    public String execute(JsonNode args) {
        String query = args.has("query") ? args.get("query").asText() : "";

        if (query.isEmpty()) {
            return "Error: query is required";
        }

        // Try Baidu first (accessible in China)
        String result = searchBaidu(query);
        if (result != null) return result;

        // Fallback: try Bing
        result = searchBing(query);
        if (result != null) return result;

        return "搜索 \"" + query + "\" 失败，所有搜索引擎均不可达。";
    }

    private String searchBaidu(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://www.baidu.com/s?wd=" + encoded + "&ie=utf-8";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .get();

            // Baidu search results
            Elements results = doc.select(".result, .c-container, .c-abstract");
            if (results.isEmpty()) {
                results = doc.select("[tpl], .result-item");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("百度搜索结果：\n\n");

            int count = 0;
            for (Element result : results) {
                if (count >= 5) break;

                String title = "";
                String snippet = "";

                Element titleEl = result.selectFirst("h3 a, .t a, a");
                if (titleEl != null) {
                    title = titleEl.text().trim();
                }

                Element snippetEl = result.selectFirst(".c-abstract, .c-span-last, .content-right_8Zs40");
                if (snippetEl != null) {
                    snippet = snippetEl.text().trim();
                }

                if (!title.isEmpty()) {
                    sb.append(++count).append(". ").append(title).append("\n");
                    if (!snippet.isEmpty()) {
                        sb.append("   ").append(snippet).append("\n");
                    }
                    sb.append("\n");
                }
            }

            if (count > 0) {
                return sb.toString().trim();
            }

            // Try parsing JSON embedded results from Baidu
            return null;

        } catch (Exception e) {
            return null; // fallback to next search engine
        }
    }

    private String searchBing(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://cn.bing.com/search?q=" + encoded + "&setlang=zh-cn";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .get();

            Elements results = doc.select(".b_algo, .b_title");
            if (results.isEmpty()) {
                results = doc.select("li.b_algo");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("搜索结果：\n\n");

            int count = 0;
            for (Element result : results) {
                if (count >= 5) break;

                String title = result.selectFirst("h2 a, a") != null
                        ? result.selectFirst("h2 a, a").text().trim() : "";
                String snippet = result.selectFirst(".b_caption p, .b_lineclamp2") != null
                        ? result.selectFirst(".b_caption p, .b_lineclamp2").text().trim() : "";

                if (!title.isEmpty()) {
                    sb.append(++count).append(". ").append(title).append("\n");
                    if (!snippet.isEmpty()) {
                        sb.append("   ").append(snippet).append("\n");
                    }
                    sb.append("\n");
                }
            }

            if (count > 0) {
                return sb.toString().trim();
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ToolDefinition getDefinition() {
        return new ToolDefinition(
                "web_search",
                "Search the internet for current information. Use this for news, weather, recent events, and general knowledge queries.",
                """
                {
                    "type": "object",
                    "properties": {
                        "query": {
                            "type": "string",
                            "description": "Search query string"
                        }
                    },
                    "required": ["query"]
                }
                """
        );
    }
}
