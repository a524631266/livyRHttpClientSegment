package com.hw.transmitlayer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.livy.LivyClient;

public class TestHttpClient {
    public static void main(String[] args) throws URISyntaxException {
        URI uri = new URI("http://192.168.40.179:8070/absc/sessions/2?abc=2#abc");
        System.out.println(uri.toString() + "==>getPath:" + uri.getPath());

        System.out.println(uri.toString() + "==>getUserInfo:" + uri.getUserInfo());
        System.out.println(uri.toString() + "==>getScheme:" + uri.getScheme());
        System.out.println(uri.toString() + "==>getQuery:" + uri.getQuery());
        System.out.println(uri.toString() + "==>getFragment:" + uri.getFragment());
        System.out.println(uri.toString() + "==>getHost:" + uri.getHost());
        System.out.println(uri.toString() + "==>getPort:" + uri.getPort());
        System.out.println("################");

        Matcher matcher = Pattern.compile("(.*)" + "/sessions" + "/([0-9]+)" + "(.*?)")
                .matcher(uri.getPath());
//        System.out.println(matcher.group(1));
        System.out.println("matcher: " + matcher.matches());

    }
}
