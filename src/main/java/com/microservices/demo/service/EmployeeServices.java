package com.microservices.demo.service;

import java.io.IOException;

public interface EmployeeServices {

    String convertToCsvEmployee() throws IOException;
}
