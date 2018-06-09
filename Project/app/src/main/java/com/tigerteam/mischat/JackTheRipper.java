package com.tigerteam.mischat;

import android.content.Context;

import com.bridgefy.sdk.client.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JackTheRipper {

    public  static void Rip(Context context, RegistrationListener listener) {
        Class<?> concreteClass = Bridgefy.class;

        /*

val 6 api key : 254540ef-f130-463c-b13d-e2d2adcb8f3a
val 7 uuid = dc2f5b08-c65b-4454-bee2-3e9c8eeb3d96
var 8 MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxsN9UtQH8G5m7NAOEvclUhkRo/eDCt9OFSmD7WRK4cKWtcoWH57522AA//DHvBPkNd4zSMIbSrPoOvaD3NPbuhB7zZ6BUTG/UCSSZTh7J53n7RELw9ntWyU2sHpeLqQuwUlkTCudBdsnukRvwIbqpqPNYvpJ7T199upfDZ8Wff1Uik7WaoRaUtzxgf1UIFhh9TDws8cwrS8wUN5kmecKjNNf+mYNYWxaRApVsraK9Pl4CAcnPD/3oHYHbA/7TEOAVFRZq6p+yLl0BuBaDzO+yZ54NkOLJ/Iux/hVwYOJJAdzn/gI7/OC80Mv8qF/MjgjZ2UqWsMc71H5YKlaFi2kXwIDAQAB
var 9 MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDGw31S1Afwbmbs0A4S9yVSGRGj94MK304VKYPtZErhwpa1yhYfnvnbYAD/8Me8E+Q13jNIwhtKs+g69oPc09u6EHvNnoFRMb9QJJJlOHsnneftEQvD2e1bJTawel4upC7BSWRMK50F2ye6RG/Ahuqmo81i+kntPX326l8NnxZ9/VSKTtZqhFpS3PGB/VQgWGH1MPCzxzCtLzBQ3mSZ5wqM01/6Zg1hbFpEClWytor0+XgIByc8P/egdgdsD/tMQ4BUVFmrqn7IuXQG4FoPM77Jnng2Q4sn8i7H+FXBg4kkB3Of+Ajv84LzQy/yoX8yOCNnZSpawxzvUflgqVoWLaRfAgMBAAECggEADXjOk8CfUSKKg16OtaZetzPPyEdMcH42MqT/tiVoFek0gdnSqg4CbWYSa0aMoyjN/QC0hSfy5ZtQED8JLPyHFhNr8MHfjftLNklkSswLPOItCBpxq7KnrdfV3xxMKeki6iLpU2hZWj5CcROWZ3w9h1Hu59iDDjtuwD+edbPkuGFGtjP/8mDkJn+Aku+tjA85JoKfcFrdYN3sOh2H/Tsn/qynPKzuazx+lUZu/4X65NfsPA7vKfLSg1b8TF0M6kO4/PJnplWSaNT15CVsUXet14Kiv0id0FcDjxSmZZTiTqjomSeMwvm4RoMkIDCTQN3UajkPxfcWTqOVSd/7JA9nAQKBgQDlvtVAlYaYrMeb/KmI02PNnuws+eX06eFoMbMztrouIDs+rDJhd5v5WeXN1i+H74tTBanEaqNtI/RTZ3mdrScie5wyPn3Ws7SeOvJ4oLSNzYYneuNN/Q16lklOd1KU8VBVLJ6Jiz2q7N+2r/tDCmR/jMQuLDh/mEFuEclu8rnrQQKBgQDdeknbi2enLGKdMHqcLEqciTpkztcbCyc4gFmFVN/LX/5CMbjaWtWw67KifWDzaccQ1tfXLxdFFqSImeQYRb6zVVqkGU67Ts1aDiFC5KRYkRpgEQ2lqAWPTelEQDRrD5rsE6wldSpNlIq94XEhclb3arz+iwxir9Lh5vL+YpXHnwKBgQDNVDyrN/fPQ7wyQSIT0zmDb+GMYNuQg+achK6wLUNALjZE0QlF6uxPcnTb/e0FBKX0UT9k+BmPVW7ri7M5pKMxQmBa11cIKBvovq4t+RLlGSYo6r9z2BHP2bkyk5oO24z8UqBuNS+sL3/Ren6JcCRZiqlaytLD1oPDp8nriqt3AQKBgHgCo7LLP0dezbDPbCDj/yoDUhj926FT0dTT964/52SUh3nm53uqbNCJZyqVNcuwLaX6RMrdQYa2VkGO4gIlCMi0H4F8FSV59dwue+d7bXJwbhJQK0zwVtLoLNZG1CCPPCtgHY/OhTW0n9l0J5EwG9/FftOPFQ8Epwix1EnzsaWRAoGBAN0Pfp8BekRa6qdUUXfDJIKFo5014uXMvMrtxStBiPXsIz52QsrE6DJcI03PWcD3skEK4sNQkJO1hu13kDWatP/PT3IUGb9sSuy8VfVsXekG05Q+yTXRS+xmIwACOrL9A9l3bw6IlyOZE9VL3UlYwjNdzrH+x0Pg4PtCt3i7NkZ4
         */

        String var6 = "254540ef-f130-463c-b13d-e2d2adcb8f3a";
        String var7 = "dc2f5b08-c65b-4454-bee2-3e9c8eeb3d96";
        String var8 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxsN9UtQH8G5m7NAOEvclUhkRo/eDCt9OFSmD7WRK4cKWtcoWH57522AA//DHvBPkNd4zSMIbSrPoOvaD3NPbuhB7zZ6BUTG/UCSSZTh7J53n7RELw9ntWyU2sHpeLqQuwUlkTCudBdsnukRvwIbqpqPNYvpJ7T199upfDZ8Wff1Uik7WaoRaUtzxgf1UIFhh9TDws8cwrS8wUN5kmecKjNNf+mYNYWxaRApVsraK9Pl4CAcnPD/3oHYHbA/7TEOAVFRZq6p+yLl0BuBaDzO+yZ54NkOLJ/Iux/hVwYOJJAdzn/gI7/OC80Mv8qF/MjgjZ2UqWsMc71H5YKlaFi2kXwIDAQAB";
        String var9 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDGw31S1Afwbmbs0A4S9yVSGRGj94MK304VKYPtZErhwpa1yhYfnvnbYAD/8Me8E+Q13jNIwhtKs+g69oPc09u6EHvNnoFRMb9QJJJlOHsnneftEQvD2e1bJTawel4upC7BSWRMK50F2ye6RG/Ahuqmo81i+kntPX326l8NnxZ9/VSKTtZqhFpS3PGB/VQgWGH1MPCzxzCtLzBQ3mSZ5wqM01/6Zg1hbFpEClWytor0+XgIByc8P/egdgdsD/tMQ4BUVFmrqn7IuXQG4FoPM77Jnng2Q4sn8i7H+FXBg4kkB3Of+Ajv84LzQy/yoX8yOCNnZSpawxzvUflgqVoWLaRfAgMBAAECggEADXjOk8CfUSKKg16OtaZetzPPyEdMcH42MqT/tiVoFek0gdnSqg4CbWYSa0aMoyjN/QC0hSfy5ZtQED8JLPyHFhNr8MHfjftLNklkSswLPOItCBpxq7KnrdfV3xxMKeki6iLpU2hZWj5CcROWZ3w9h1Hu59iDDjtuwD+edbPkuGFGtjP/8mDkJn+Aku+tjA85JoKfcFrdYN3sOh2H/Tsn/qynPKzuazx+lUZu/4X65NfsPA7vKfLSg1b8TF0M6kO4/PJnplWSaNT15CVsUXet14Kiv0id0FcDjxSmZZTiTqjomSeMwvm4RoMkIDCTQN3UajkPxfcWTqOVSd/7JA9nAQKBgQDlvtVAlYaYrMeb/KmI02PNnuws+eX06eFoMbMztrouIDs+rDJhd5v5WeXN1i+H74tTBanEaqNtI/RTZ3mdrScie5wyPn3Ws7SeOvJ4oLSNzYYneuNN/Q16lklOd1KU8VBVLJ6Jiz2q7N+2r/tDCmR/jMQuLDh/mEFuEclu8rnrQQKBgQDdeknbi2enLGKdMHqcLEqciTpkztcbCyc4gFmFVN/LX/5CMbjaWtWw67KifWDzaccQ1tfXLxdFFqSImeQYRb6zVVqkGU67Ts1aDiFC5KRYkRpgEQ2lqAWPTelEQDRrD5rsE6wldSpNlIq94XEhclb3arz+iwxir9Lh5vL+YpXHnwKBgQDNVDyrN/fPQ7wyQSIT0zmDb+GMYNuQg+achK6wLUNALjZE0QlF6uxPcnTb/e0FBKX0UT9k+BmPVW7ri7M5pKMxQmBa11cIKBvovq4t+RLlGSYo6r9z2BHP2bkyk5oO24z8UqBuNS+sL3/Ren6JcCRZiqlaytLD1oPDp8nriqt3AQKBgHgCo7LLP0dezbDPbCDj/yoDUhj926FT0dTT964/52SUh3nm53uqbNCJZyqVNcuwLaX6RMrdQYa2VkGO4gIlCMi0H4F8FSV59dwue+d7bXJwbhJQK0zwVtLoLNZG1CCPPCtgHY/OhTW0n9l0J5EwG9/FftOPFQ8Epwix1EnzsaWRAoGBAN0Pfp8BekRa6qdUUXfDJIKFo5014uXMvMrtxStBiPXsIz52QsrE6DJcI03PWcD3skEK4sNQkJO1hu13kDWatP/PT3IUGb9sSuy8VfVsXekG05Q+yTXRS+xmIwACOrL9A9l3bw6IlyOZE9VL3UlYwjNdzrH+x0Pg4PtCt3i7NkZ4";

        Method method1 = null;
        Method method2 = null;
        try {
            Method[] a = concreteClass.getDeclaredMethods();

            for(Method m : a)
            {
                if(m.toString().equals("private static void com.bridgefy.sdk.client.Bridgefy.b(android.content.Context,java.lang.String,java.lang.String,java.lang.String,java.lang.String)"))
                {
                    method1 = m;
                }
                else if (m.toString().equals("private static void com.bridgefy.sdk.client.Bridgefy.b(com.bridgefy.sdk.client.RegistrationListener,com.bridgefy.sdk.client.BridgefyClient)"))
                {
                    method2 = m;
                }
            }



            method1.setAccessible(true);
            method1.invoke(null, context, var7, var6, var8, var9);



            method2.setAccessible(true);
            method2.invoke(null, listener, Bridgefy.getInstance().getBridgefyClient());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
