package in.dataman.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @PostMapping("/extract-text")
    public String extractText(@RequestParam("file") MultipartFile file) throws IOException {
        // Create a Tesseract instance
        Tesseract tesseract = new Tesseract();

        // Set the Tesseract data path for local execution
        String localTessDataPath = new File("src/main/resources/Tesseract-OCR/tessdata").getAbsolutePath();
        tesseract.setDatapath(localTessDataPath);

        // Optional: set the language you want to recognize, e.g., "eng" for English
        tesseract.setLanguage("eng");

        // Convert MultipartFile to File for processing
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);

        try {
            // Extract text from image
            return tesseract.doOCR(convFile);
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error reading image";
        }
    }
}
