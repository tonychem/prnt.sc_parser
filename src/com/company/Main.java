package com.company;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        for (int i = 200; i < 2000; i++) {
            URI uri = URI.create("https://prnt.sc/dc" + number4(i));
            System.out.println(number4(i));
            HttpResponse<String> response = client.send(request(uri), HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            Pattern p = Pattern.compile("https://image\\.prntscr\\.com/.+?\\.png");
            Matcher m = p.matcher(body);
            if (m.find()) {
                URL url = URI.create(m.group()).toURL();
                try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                     FileOutputStream fos = new FileOutputStream("C:\\Users\\Anatoly\\IdeaProjects\\screenshotparser\\download\\" + i + ".png")) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static HttpRequest request(URI suffix) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(suffix)
                .GET()
                .build();
        return request;
    }

    public static String number4(int i) {
        if (i >= 1000) {
            return String.valueOf(i);
        }

        if (i >= 100 & i < 1000) {
            return String.format("0%d", i);
        }

        if (i >= 10 & i < 100) {
            return String.format("00%d", i);
        }

        if (i >= 0 & i < 10) {
            return String.format("000%d", i);
        }

        return "0000";
    }
}
