// package in.dataman.controller;
// import net.sourceforge.tess4j.Tesseract;
// import net.sourceforge.tess4j.TesseractException;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.File;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.StandardCopyOption;

// @RestController
// @RequestMapping("/api/ocr")
// public class OCRController {

//     @PostMapping("/extract-text")
//     public String extractText(@RequestParam("file") MultipartFile file) throws IOException {
//         // Create a Tesseract instance
//         Tesseract tesseract = new Tesseract();

//         // Detect if running on Heroku or local environment
//         String tessDataPath;
//         if (System.getenv("DYNO") != null) {
//             // Heroku environment - use tessdata from resources
//             Path tempDir = Files.createTempDirectory("tessdata");
//             Files.copy(getClass().getResourceAsStream("/Tesseract-OCR/tessdata/eng.traineddata"),
//                     tempDir.resolve("eng.traineddata"), StandardCopyOption.REPLACE_EXISTING);
//             tessDataPath = tempDir.toString();
//         } else {
//             // Local environment
//             tessDataPath = new File("src/main/resources/Tesseract-OCR/tessdata").getAbsolutePath();
//         }
//         tesseract.setDatapath(tessDataPath);

//         // Optional: set the language you want to recognize, e.g., "eng" for English
//         tesseract.setLanguage("eng");

//         // Convert MultipartFile to File for processing
//         File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
//         file.transferTo(convFile);

//         try {
//             // Extract text from image
//             return tesseract.doOCR(convFile);
//         } catch (TesseractException e) {
//             e.printStackTrace();
//             return "Error reading image";
//         }
//     }
// }



package in.dataman.controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@RequestMapping("/apii/ocr")
public class OCRController {

    @PostMapping("/extract-info")
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) throws IOException {
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
        	String text = tesseract.doOCR(convFile);
        	
        	System.out.println(parseAadharNumber(text));
        	System.out.println(parseName(text));
        	System.out.println(parseDOB(text));
        	System.out.println(parseGender(text));
        	
        	Map<String, String> parsedInfo = new HashMap<>();
        	
        	parsedInfo.put("aadhar_number", parseAadharNumber(text));
            parsedInfo.put("name", parseName(text));
            parsedInfo.put("dob", parseDOB(text));
            parsedInfo.put("gender", parseGender(text));
        	
            //return text;
            return ResponseEntity.ok(parsedInfo);
        } catch (TesseractException e) {
            e.printStackTrace();
            //return "Error reading image";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the image: " + e.getMessage());
        }
    }
    
    private String parseAadharNumber(String text) {
        String aadharPattern = "\\d{4}\\s*\\d{4}\\s*\\d{4}";
        Pattern pattern = Pattern.compile(aadharPattern);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group().replaceAll("\\s+", "");
        }
        return "Aadhar number not found";
    }

    private String parseName(String text) {
        String[] namePatterns = { "Name[:\\s]*([A-Za-z\\s]+)", "Name\\s*[:\\s]*([A-Za-z\\s]+)",
                "Name:\\s*([A-Za-z\\s]+)", "Name\\s*([A-Za-z\\s]+)", "([A-Z][a-z]+(?:\\s[A-Z][a-z]+)*)" };

        for (String namePattern : namePatterns) {
            Pattern pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return "Name not found";
    }

    private String parseDOB(String text) {
        String dobPattern = "DOB[:\\s]*(\\d{2}/\\d{2}/\\d{4})";
        Pattern pattern = Pattern.compile(dobPattern);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Date of Birth not found";
    }

    private String parseGender(String text) {
        String genderPattern = "(Male|Female|MALE|FEMALE)";
        Pattern pattern = Pattern.compile(genderPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return "Gender not found";
    }
}
