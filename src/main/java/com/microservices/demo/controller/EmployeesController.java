package com.microservices.demo.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microservices.demo.service.EmployeeServices;

import java.net.URISyntaxException;

@RestController
public class EmployeesController {

    private final Logger logger = LoggerFactory.getLogger(EmployeesController.class);

    @Autowired
    EmployeeServices employeeServices;

    @CrossOrigin
    @GetMapping(value="/employees",  produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> employeesToCsv() throws URISyntaxException {

        logger.debug("REST request to get InitBoard");

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = new ResponseEntity<>(employeeServices.convertToCsvEmployee(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }
}
