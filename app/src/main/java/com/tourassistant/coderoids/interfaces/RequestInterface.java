package com.tourassistant.coderoids.interfaces;

public interface RequestInterface {
    void RequestFinished(String fragmentName,String apiName,String responseString);
    void RequestSecureFinished(String fragmentName,String apiName,String responseString);
}
