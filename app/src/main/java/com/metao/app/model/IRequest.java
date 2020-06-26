package com.metao.app.model;

public interface IRequest {

    public int getDelay();

    public String getRequestString();

    public String getUUID();

    //get RequestBit to identify
    public String getRequestBit();

    //expected to update the textviews and the statistic class
    public String handleResponse(String[] request);

    public RequestType getRequestType();

}
