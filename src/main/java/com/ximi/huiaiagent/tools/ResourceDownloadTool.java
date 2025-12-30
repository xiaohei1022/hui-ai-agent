package com.ximi.huiaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.ximi.huiaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class ResourceDownloadTool {

    /**
     * 下载资源
     * @param url
     * @param fileName
     * @return
     */
    @Tool(description = "Download a resource from a URL")
    public String downloadResource(@ToolParam (description = "URL of the resource to download") String url,
                                   @ToolParam (description = "Name of the file to save the download resource") String fileName) {
        // 使用HttpClient或其他HTTP库下载资源
        String fileDir = FileConstant.FILE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;

        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, filePath);
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            // 返回下载的资源内容
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
