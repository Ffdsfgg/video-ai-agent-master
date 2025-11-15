package com.lilei.leiaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.lilei.leiaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * PDF Generation Tool
 */
public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content and metadata", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content,
            @ToolParam(description = "Title of the PDF") String title,
            @ToolParam(description = "Author of the PDF") String author) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // Create directory
            FileUtil.mkdir(fileDir);
            // Create PdfWriter and PdfDocument objects
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // Add metadata
                PdfDocumentInfo info = pdf.getDocumentInfo();
                info.setTitle(title);
                info.setAuthor(author);

                // Use built-in Chinese font
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                // Add content
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);

                // Add page numbers
                int totalPages = pdf.getNumberOfPages();
                for (int i = 1; i <= totalPages; i++) {
                    document.add(new Paragraph(new Text("Page " + i + " of " + totalPages))
                            .setFontSize(10)
                            .setFixedPosition(i, 500, 20, 100));
                }
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }
}