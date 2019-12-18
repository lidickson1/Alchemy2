package main.exceptions;

class NoGroupException extends Exception {

    public NoGroupException(String group) {
        super(group + " not found!");
    }

}
