package org.data;

import com.jayway.jsonpath.JsonPath;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SpotifyTopSongs {

    public static void main(String[] args) {
        // Your Spotify application credentials
        String clientId = "CLIENT-ID";
        String clientSecret = "CLIENT-SECRET";
        String playlistId="PLAYLIST-ID"; //37i9dQZEVXbLZ52XmnySJg

        // Base64 encode client ID and client secret
        String encodedCredentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        // Spotify Accounts service endpoint
        String accessTokenUrl = "https://accounts.spotify.com/api/token";

        // Set up request parameters
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        // Request body parameters
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "client_credentials");

        // Convert Map to MultiValueMap
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestBody.forEach(requestParams::add);

        // Create the HTTP entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestParams, headers);

        // Use RestTemplate to send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                accessTokenUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Check if the request was successful
        if (response.getStatusCode().is2xxSuccessful()) {
            // Extract access token from response body
            String responseBody = response.getBody();
            String accessToken = responseBody.substring(responseBody.indexOf(":") + 2, responseBody.indexOf(",") - 1);

            // Use the access token to retrieve the top tracks
            HttpHeaders headersTopTracks = new HttpHeaders();
            headersTopTracks.setBearerAuth(accessToken);
            HttpEntity<String> entityTopTracks = new HttpEntity<>(headersTopTracks);
            ResponseEntity<String> responseTopTracks = restTemplate.exchange(
                    "https://api.spotify.com/v1/playlists/"+playlistId,
                    HttpMethod.GET,
                    entityTopTracks,
                    String.class
            );
            List<Data> list = new ArrayList<>();
            // Check if the request for top tracks was successful
            if (responseTopTracks.getStatusCode().is2xxSuccessful()) {
                for (int i = 0; i <= 49; i++) {
                    String singer = JsonPath.read(responseTopTracks.getBody(), "tracks.items[" + i + "].track.album.artists[0].name");
                    String song = JsonPath.read(responseTopTracks.getBody(), "tracks.items[" + i + "].track.name");
                    Data data = new Data(singer, song);
                    list.add(data);
                }
                exportToExcel(list);
            } else {
                System.out.println("Error: " + responseTopTracks.getStatusCode() + " " + responseTopTracks.getStatusCode().getReasonPhrase());
            }
        } else {
            System.out.println("Error: " + response.getStatusCode() + " " + response.getStatusCode().getReasonPhrase());
        }
    }

    private static void exportToExcel(List<Data> tracks) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Playlist Tracks");
            int rowNum = 0;
            for (Data track : tracks) {
                Row row=sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(track.getSong());
                row.createCell(1).setCellValue(track.getSinger());
            }

            // Write the workbook content to a file
            try (FileOutputStream fileOut = new FileOutputStream(new File("playlist_tracks.xlsx"))) {
                workbook.write(fileOut);
                System.out.println("Excel file exported successfully.");
            }
        } catch (IOException e) {
            System.out.println("Error exporting to Excel: " + e.getMessage());
        }
    }
}

