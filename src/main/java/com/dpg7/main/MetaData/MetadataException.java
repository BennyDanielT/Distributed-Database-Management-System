package com.dpg7.main.MetaData;

public final class MetadataException extends Exception
{

    private final String errorMessage;

    public MetadataException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "MetaDataException{" +
                "Exception" + errorMessage + '\'' +
                '}';
    }
}