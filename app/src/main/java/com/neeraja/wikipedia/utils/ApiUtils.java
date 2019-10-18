package com.neeraja.wikipedia.utils;

public class ApiUtils {
    public static final String baseUrl = "https://en.wikipedia.org//w/api.php?action=query&format=json&prop=pageimages%7Cpageterms&generator=prefixsearch&redirects=1&formatversion=2&piprop=thumbnail&pithumbsize=50&pilimit=10&wbptterms=description&gpslimit=10&gpssearch=";
    public static String getSearchUrl(String query) {
        return baseUrl + query;
    }
}
