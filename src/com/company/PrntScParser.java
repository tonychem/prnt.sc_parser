package com.company;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrntScParser {

    private HttpClient client;
    private Scanner scn;
    private String prefix;
    private int from;
    private int to;
    boolean inLoop;
    private Path workingDirectory;

    public PrntScParser() {
        client = HttpClient.newHttpClient();
        scn = new Scanner(System.in);
        prefix = "";
        inLoop = true;
        workingDirectory = Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator")
                + "download");
    }

    public static void main(String[] args) throws IOException {
        PrntScParser parser = new PrntScParser();
        parser.gatherUserData();
        parser.downloadData();
    }

    public void gatherUserData() {
        while (inLoop) {
            try {
                System.out.print("От: ");
                from = scn.nextInt();
                System.out.print("До: ");
                to = scn.nextInt();
                scn.nextLine();
                System.out.print("Префикс: ");
                prefix = scn.nextLine();
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }
            inLoop = !((from > -1) && (to > -1) && (from <= to) && prefix.matches("[a-z]{2}"));
            if (inLoop) {
                System.out.println("Длина префикса - две прописные латинские буквы, числа - от 0 до 9999");
            }
        }
    }

    public void downloadData() throws IOException {
        if (!Files.exists(workingDirectory)) {
            Files.createDirectory(workingDirectory);
        }
        System.out.println("Downloading to: " + workingDirectory);
        for (int i = from; i <= to; i++) {
            String prefixAndNumber = prefix + number4(i);
            URI uri = URI.create("https://prnt.sc/" + prefixAndNumber);
            System.out.println("Downloading: " + prefixAndNumber + ".png");
            try {
                HttpResponse<String> response = client.send(request(uri), HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                String address = findPictureURLAddress(body);
                saveAsFile(address,
                        workingDirectory.toString() + System.getProperty("file.separator") +
                                prefixAndNumber + ".png");
            } catch (RuntimeException | IOException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static HttpRequest request(URI suffix) {
        return HttpRequest.newBuilder()
                .uri(suffix)
                .GET()
                .build();
    }

    private static void saveAsFile(String source, String filePathName) throws MalformedURLException {
        URL url = URI.create(source).toURL();
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(filePathName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String findPictureURLAddress(String requestBody) {
        Pattern p = Pattern.compile("https://image\\.prntscr\\.com/.+?\\.png");
        Matcher m = p.matcher(requestBody);

        if (m.find()) {
            return m.group();
        } else {
            throw new RuntimeException("URL NOT FOUND");
        }
    }

    public static String number4(int i) {
        return String.format("%04d", i);
    }
}
