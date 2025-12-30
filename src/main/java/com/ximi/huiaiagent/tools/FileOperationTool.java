package com.ximi.huiaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.ximi.huiaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private String FILE_DIR = FileConstant.FILE_DIR + "/file";

    /**
     * 读取文件内容
     * @return
     */
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileNAme) {
        String filePath = FILE_DIR + "/" + fileNAme;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }


    /**
     * 写入文件
     * @param fileName
     * @param content
     * @return
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File write successfuly to: " + filePath;
        } catch (IORuntimeException e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}
