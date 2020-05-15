package com.microservices.demo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.microservices.demo.service.EmployeeServices;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmployeeImpl implements EmployeeServices {

    private final Logger logger = LoggerFactory.getLogger(EmployeeImpl.class);

    @Override
    public String convertToCsvEmployee() throws IOException {

        File csvOutputFile = new File("prueba.csv");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://dummy.restapiexample.com/api/v1/employees");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            HttpEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.readValue(response.getBody(), ObjectNode.class);
            if(node.get("data").isArray()) {
                List<String[]> dataLines = new ArrayList<>();
                node.get("data").forEach(c -> {
                    dataLines.add(new String[]
                            { c.get("id").asText(), c.get("employee_name").asText(), c.get("employee_salary").asText(), c.get("employee_age").asText(), c.get("profile_image").asText()});
                });


                try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                    dataLines.stream()
                            .map(this::convertToCSV)
                            .forEach(pw::println);
                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage(),e);
                }

            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return uploadFile(csvOutputFile);
    }

    public String uploadFile(File csvOutputFile) throws IOException {

        Storage storage = StorageOptions.getDefaultInstance().getService();

        final String fileName = csvOutputFile.getName();
        InputStream is = new FileInputStream(csvOutputFile);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] readBuf = new byte[4096];
        while (is.available() > 0) {
            int bytesRead = is.read(readBuf);
            os.write(readBuf, 0, bytesRead);
        }

        // Convert ByteArrayOutputStream into byte[]
        BlobInfo blobInfo =
                storage.create(
                        BlobInfo
                                .newBuilder("tests", fileName)
                                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                                .build(),
                        os.toByteArray());
        return blobInfo.getMediaLink();
    }



    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }
}