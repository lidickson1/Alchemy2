package main.exceptions;

public class NoElementException extends Exception {

    public NoElementException(String element) {
        super(element + " not found!");
    }

}
