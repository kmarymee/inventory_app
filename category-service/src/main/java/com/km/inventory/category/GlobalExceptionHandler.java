package com.km.inventory.category;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {


    //Tells rest controller what to do in the event a ProductNotFound exception is thrown
    @ExceptionHandler(ResourceNotFoundException.class)
    //We're going to return a Response Entity that comes with a map of strings as they keys and values, to be used as JSON.
    // An instance of the exeption we're planning for will come in as an argument
    public ResponseEntity<Map<String,String>> handleNotFound(ResourceNotFoundException ex) {
        //We will create a map that will serve as the JSON body.
        Map<String,String> body = new HashMap<>();
        //We'll pull the message field from the exeption and label it 'error' in the json body.
        body.put("error", ex.getMessage());
        //We'll use the ResponseEntity class to create a new ResponseEntity with a 404 status, 
        // and send our body variable in to be turned into JSON
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    //When the rest controller throws a MethodArgumentNotValidException, we'll do the following:
    @ExceptionHandler(MethodArgumentNotValidException.class)
    //Again, we'll prepare to return a ResponseEntity with a Map that represents the JSON,
    // and take in the exception. 
    public ResponseEntity<Map<String,String>> handleValidation(MethodArgumentNotValidException ex) {
        // A map representing the response body
        Map<String,String> errors = new HashMap<>();
        // This exception will have multiple fields for errors, so for each one...
        ex.getBindingResult().getFieldErrors().forEach(error->
            //We'll store the keys and values in our map.
            errors.put(error.getField(), error.getDefaultMessage())
        );
        //We can now return the response.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    //This is my own addition
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> handleNotReadable(HttpMessageNotReadableException ex) {
        Map<String,String> errors = new HashMap<>();
        
        errors.put("error","HTTP message is not readable.");
        errors.put("DEBUG", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}

