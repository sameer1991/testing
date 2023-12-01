import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.*;



public class PRCount {

    public static void main(String[] args) {
        String owner = "sameer1991";
        String repo = "testing";
        String accessToken = "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"; // Replace with your GitHub access token
        
        int failed_merged[]= getMergedPullRequestCountForToday(owner,repo,accessToken);
        int mergedCount = failed_merged[0];
        int failedCount = failed_merged[1];
        int raisedCount = failed_merged[2];

        System.out.println("Merged PR count: " + mergedCount);
        System.out.println("Failed PR count: " + failedCount);
        System.out.println("Raised PR count: " + raisedCount);

    }

    public static int[] getMergedPullRequestCountForToday(String owner, String repo, String accessToken) {
        ZonedDateTime gmtDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        String gmtDate = gmtDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String current_date = gmtDate.substring(0, 10);
        int count[]=new int[3];
        int merged_count=0;
        int failed_count=0;
        int raised_count=0;

        try {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayDate = today.format(formatter);

            int page = 1;
            int perPage = 100; // GitHub API max per page
            boolean hasMorePages;

            do {
                String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls?state=all&page=" + page + "&per_page=" + perPage;
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                // Merged & Failed PR Count
                String jsonResponse = response.toString();
                String[] prArray = jsonResponse.split("\"state\":\"closed\"");
                for (String pr : prArray) {
                    if (pr.contains("\"merged_at\":")) {
                        String mergedDateString = pr.split("\"merged_at\":")[1].split(",")[0].trim();
                        String ClosedDateString = pr.split("\"closed_at\":")[1].split(",")[0].trim();
                        if((ClosedDateString!=null && !ClosedDateString.equals("null"))){
                            if (mergedDateString != null && !mergedDateString.equals("null")) {
                                String merged_date = mergedDateString.substring(1, 11);
                                if((current_date.equals(merged_date))==true){
                                    merged_count++;
                                }
                            }else{
                                failed_count++;
                            }
                        }

                    }
                }
                // Raised PR's Count
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    String[] raisedPR = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String value = jsonObject.optString("created_at", "");
                        raisedPR[i] = value;
                    }
                    for (String value : raisedPR) {
                        String created_date = value.substring(0, 10);
                        if ((current_date.equals(created_date)) == true) {
                            raised_count++;
                        }
                    }
                hasMorePages = jsonResponse.contains("rel=\"next\"");
                page++;

            } while (hasMorePages);

        } catch (IOException e) {
            e.printStackTrace();
        }
        count[0]=merged_count;
        count[1]=failed_count;
        count[2]=raised_count;
        return count;
    }


}
