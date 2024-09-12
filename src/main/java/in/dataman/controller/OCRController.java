package in.dataman.controller;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @PostMapping("/extract-text")
    public String extractText(@RequestParam("file") MultipartFile file) throws IOException {
        // Create a Tesseract instance
        Tesseract tesseract = new Tesseract();

        // Detect if running on Heroku or local environment
        String tessDataPath;
        if (System.getenv("DYNO") != null) {
            // Heroku environment - use tessdata from resources
            Path tempDir = Files.createTempDirectory("tessdata");
            Files.copy(getClass().getResourceAsStream("/Tesseract-OCR/tessdata/eng.traineddata"),
                    tempDir.resolve("eng.traineddata"), StandardCopyOption.REPLACE_EXISTING);
            tessDataPath = tempDir.toString();
        } else {
            // Local environment
            tessDataPath = new File("src/main/resources/Tesseract-OCR/tessdata").getAbsolutePath();
        }
        tesseract.setDatapath(tessDataPath);

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
