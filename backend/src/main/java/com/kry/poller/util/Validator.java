package com.kry.poller.util;

public class Validator {
    public static final String URL_REGEX =
      "^((https?://)(www.)?[a-z0-9]+(\\.[a-z]{2,}){1,3}(#?/?[a-zA-Z0-9#]+)*/?(\\?[a-zA-Z0-9-_]+=[a-zA-Z0-9-%]+&?)?|" +
      "(https?://)?[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}:?.*)|" +
      "(https?://)localhost:?.*$";
    public static Boolean validateUrl(String url) {
        return url.matches(URL_REGEX);
    }
}
