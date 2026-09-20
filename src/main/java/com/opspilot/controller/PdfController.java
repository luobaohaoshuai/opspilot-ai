package com.opspilot.controller;

import com.opspilot.common.result.ErrorCode;
import com.opspilot.common.result.Result;
import com.opspilot.service.PdfService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/pdf")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 检查是否为 PDF
        String filename = file.getOriginalFilename();
        if (file.isEmpty() || filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return Result.fail(ErrorCode.BAD_REQUEST, "只允许上传 PDF 文件");
        }

        // 2. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail(ErrorCode.BAD_REQUEST, "文件大小不能超过 10MB");
        }

        try {
            String text = pdfService.extractText(file);
            String documentName = file.getOriginalFilename();
            List<String> chunks = pdfService.splitText(text, 500, 50);
            pdfService.embedAndStore(chunks, documentName);
            return Result.ok("提取成功，共 " + text.length() + " 字符，切成 " + chunks.size() + " 段\n"
                    + "前 500 字：\n" + text.substring(0, Math.min(500, text.length())));
        } catch (Exception e) {
            return Result.fail(ErrorCode.INTERNAL_ERROR, "文档处理失败");
        }
    }

    @GetMapping("/ask")
    public Result<String> ask(@RequestParam("question") String question) {
        return Result.ok("最相关的片段：\n" + pdfService.search(question));
    }

    @GetMapping("/chat")
    public Result<PdfService.RagAnswer> chat(@RequestParam("question") String question) {
        return Result.ok(pdfService.askWithLLM(question));
    }

    @GetMapping("/documents")
    public Result<List<String>> listDocuments() {
        return Result.ok(pdfService.getDocumentList());
    }

    @DeleteMapping("/documents/{documentName}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteDocument(@PathVariable String documentName) {
        pdfService.deleteDocument(documentName);
        return Result.ok("文档已删除");
    }
}
