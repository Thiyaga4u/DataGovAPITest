package com.store.qa.test.service;

/**
 * Created by TARAMU on 5/20/2016.
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.qa.test.beans.AltFuelStationBean;
import com.store.qa.test.beans.FuelStationBean;
import com.store.qa.test.beans.FuelStationResponseBean;
import com.store.qa.test.util.WebClientDevWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestDataGovAPI {
public String stationIdToBePassed;
    private HttpClient client;

    @Test
    public void getStationID(ITestContext context) throws JSONException, IOException, URISyntaxException {

        URI loginUri = new URI("https://api.data.gov/nrel/alt-fuel-stations/v1/nearest.json?api_key=PwYYVyONrpLQt7XzcPKzISKwdU4fBw7LVgCmbJaf&location=Austin&ev_network=ChargePoint%20Network");

        this.client = new DefaultHttpClient();
        this.client = WebClientDevWrapper.wrapClient(client);

        HttpGet httpget = new HttpGet(String.valueOf(loginUri));
        HttpResponse response = this.client.execute(httpget);
        HttpEntity entity = response.getEntity();

        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.contains("HYATT AUSTIN"));
        JSONObject jsonResponse = new JSONObject(responseString);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        FuelStationResponseBean responseBean = mapper.readValue(jsonResponse.toString(), FuelStationResponseBean.class);


        FuelStationBean[] fuelStationBeans = responseBean.getFuel_stations();
        String stationId = null;
        for (FuelStationBean fuelStationBean : fuelStationBeans) {
            if (fuelStationBean.getStation_name().equals("HYATT AUSTIN")) {
                stationId = fuelStationBean.getId();
            }
        }
        context.setAttribute(stationIdToBePassed, stationId);
    }


    @Test
    public void getStationStreetAddress(ITestContext context) throws JSONException, IOException, URISyntaxException {

        String stationId = (String) context.getAttribute(stationIdToBePassed);
        URI loginUri = new URI("https://api.data.gov/nrel/alt-fuel-stations/v1/"+stationId+".json?api_key=PwYYVyONrpLQt7XzcPKzISKwdU4fBw7LVgCmbJaf");

        this.client = new DefaultHttpClient();
        this.client = WebClientDevWrapper.wrapClient(client);

        HttpGet httpget = new HttpGet(String.valueOf(loginUri));
        HttpResponse response = this.client.execute(httpget);
        HttpEntity entity = response.getEntity();

        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.contains("HYATT AUSTIN"));
        JSONObject jsonResponse = new JSONObject(responseString);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AltFuelStationBean responseBean = mapper.readValue(jsonResponse.toString(),AltFuelStationBean.class);
        FuelStationBean responseBeanFuelStation = responseBean.getAlt_fuel_station();
        String finalAddress = responseBeanFuelStation.getStreet_address() + ", " + responseBeanFuelStation.getCity() + ", " + responseBeanFuelStation.getState() + ", USA, " + responseBeanFuelStation.getZip();
        Assert.assertEquals(finalAddress, "208 Barton Springs Rd, Austin, TX, USA, 78704");

    }
}
