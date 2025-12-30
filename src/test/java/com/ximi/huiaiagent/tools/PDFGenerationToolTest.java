package com.ximi.huiaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "AI大模型学习路线.pdf";
        String content = "你好！想学AI大模型，这份路线图能帮你快速理清方向。核心是\u200C分阶段学习+实战驱动\u200C，从基础到进阶，一步步掌握开发能力。\n" +
                "一、学习阶段划分\n" +
                "1.\n" +
                "\u200C基础阶段\u200C：掌握Python编程、数学基础（线性代数、概率统计、微积分）和机器学习基础。\n" +
                "2.\n" +
                "\u200C入门阶段\u200C：学习深度学习框架（如PyTorch、TensorFlow）、NLP基础和大模型原理（如Transformer架构）。\n" +
                "3.\n" +
                "\u200C实战阶段\u200C：通过项目实践（如智能客服、知识库问答）巩固知识，学习微调技术（如LoRA）和部署方法。\n" +
                "4.\n" +
                "\u200C进阶阶段\u200C：深入NLP、计算机视觉，探索强化学习和生成模型，参与多模态项目（如文生图）。\n" +
                "二、学习资源推荐\n" +
                "\u200C视频课程\u200C：\n" +
                "AI大模型应用开发学习路径（B站）\n" +
                "AI大模型学习路线（B站）\n" +
                "5分钟讲清楚大模型学习路线（B站）\n" +
                "\u200C文章指南\u200C：\n" +
                "2025年AI大模型终极学习路线（CSDN）\n" +
                "2026大模型学习路线图（CSDN）\n" +
                "AI大模型学习路线（CSDN）\n" +
                "三、学习建议\n" +
                "\u200C理论结合实践\u200C：学完基础后，立即动手做项目，比如用LangChain框架开发智能问答系统。\n" +
                "\u200C关注行业动态\u200C：大模型技术更新快，多关注论文和开源项目，保持学习。\n" +
                "\u200C加入社区\u200C：参与AI论坛、技术群，和同行交流，能快速解决问题。";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}

