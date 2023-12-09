import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.*;



public class PR_final {

    public static void main(String[] args) {


        /*String owner = "sameer1991";
        String repo = "testing";
        String accessToken = "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"; // Replace with your GitHub access token
        String branchName="PR";
*/
        String owner="CQ";
        String repo="servicepack";
        String branchName = "release/650";
        String accessToken="";

        int count[]= getMergedPullRequestCountForToday(owner,repo,branchName,accessToken);
        int mergedCount = count[0];
        int failedCount = count[1];
        int raisedCount = count[2];

        System.out.println("Merged PR count: " + mergedCount);
        System.out.println("Failed PR count: " + failedCount);
        System.out.println("Raised PR count: " + raisedCount);

    }

    public static int[] getMergedPullRequestCountForToday(String owner, String repo, String branchName, String accessToken) {
        ZonedDateTime gmtDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        String gmtDate = gmtDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        //String current_date = gmtDate.substring(0, 10);
         String current_date = "2023-12-08";
        //System.out.println(current_date);
        int count[]=new int[3];
        int merged_count=0;
        int failed_count=0;
        int raised_count=0;

        try {
                //String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls?state=all&head=origin/PR";
                String apiUrl = "https://git.corp.adobe.com/api/v3/repos/" + owner + "/" + repo + "/pulls?state=all&head=release/650";
                //System.out.println(apiUrl);

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

                // Merged PR
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
                            }
                        }
                    }
                }
                //Failed PR
               for (String pr : prArray) {
                    if (pr.contains("\"merged_at\":")) {
                        String mergedDateString = pr.split("\"merged_at\":")[1].split(",")[0].trim();
                        String ClosedDateString = pr.split("\"closed_at\":")[1].split(",")[0].trim();
                        if((ClosedDateString!=null && !ClosedDateString.equals("null"))){
                            if (mergedDateString == null || mergedDateString.equals("null")) {
                                String closed_date = ClosedDateString.substring(1, 11);
                                if((current_date.equals(closed_date))==true){
                                    failed_count++;
                                }
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        count[0]=merged_count;
        count[1]=failed_count;
        count[2]=raised_count;
        return count;
    }


}
