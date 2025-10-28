package com.school.web.common;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
public class Response<T> {

    public T data;
    public Error error;
    public List<Error> errors;
    public HttpStatus status;
}
