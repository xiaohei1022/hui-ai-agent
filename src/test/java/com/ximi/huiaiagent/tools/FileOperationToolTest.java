package com.ximi.huiaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "zhanyan.txt";
        String content = fileOperationTool.readFile(fileName);
        assertNotNull(content);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "zhanyan.txt";
        String content = "hello world";
        String result = fileOperationTool.writeFile(fileName, content);
        assertNotNull(result);
    }
}