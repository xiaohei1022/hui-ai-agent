package com.ximi.huiaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    /**
     * 抓取网页内容
     * @param url
     * @return
     */
    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam (description = "URL of the web page") String url) {
        // 使用网页抓取工具（如Jsoup）从指定URL抓取网页内容
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
