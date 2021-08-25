package com.edument.assignment.DMS.controller;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import com.edument.assignment.DMS.model.DMSFileProperties;
import com.edument.assignment.DMS.service.DMSFileStorageService;

@RestController
public class DMSFileUploadController {
	private static final Logger logger = LoggerFactory.getLogger(DMSFileUploadController.class);
	
    @Autowired
    private DMSFileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public DMSFileProperties uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/downloadFile/")
            .path(fileName)
            .toUriString();

        return new DMSFileProperties(fileName, fileDownloadUri,
            file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List < DMSFileProperties > uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
            .stream()
            .map(file -> uploadFile(file))
            .collect(Collectors.toList());
    }
    
    @GetMapping("/fileUploadStatus/{fileName:.+}")
    public ResponseEntity<String> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        long fileSize=0;
        long docSize = 0;
        File file = null;
        // Try to determine file's content type
        String contentType = null;
        try {
        	docSize=resource.contentLength();
        	fileSize=fileStorageService.getFileSize(fileName);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
        
        System.out.println("docSize:"+docSize +"len: "+fileSize);
        if (docSize==fileSize)
        {
        	
	        return ResponseEntity.ok()
	        		.header(HttpHeaders.CONTENT_DISPOSITION, "File Upload is COMPLETE; filename=\"" + resource.getFilename() + "\"")
	                .body("File Upload is COMPLETE");
        }
        else if (docSize<=fileSize)
        {   
        	return ResponseEntity.ok()
            		.header(HttpHeaders.CONTENT_DISPOSITION, "File Upload is PENDING; filename=\"" + resource.getFilename() + "\"")
                    .body("File Upload is PENDING");
        }
        else if (docSize<=0)
        {   
        	return ResponseEntity.ok()
            		.header(HttpHeaders.CONTENT_DISPOSITION, "File Upload is NOT_STARTED; filename=\"" + resource.getFilename() + "\"")
                    .body("File Upload is NOT_STARTED");
        }
	        
        return ResponseEntity.ok()
	        		.header(HttpHeaders.CONTENT_DISPOSITION, "File Upload is NOT_STARTED; filename=\"" + resource.getFilename() + "\"")
	                .body("File Upload is NOT_STARTED");
    }
}