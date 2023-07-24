import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
public class JsonPlaceholderAPI {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        // Example usage
        JsonObject newUser = new JsonObject();
        newUser.addProperty("name", "John Doe");
        newUser.addProperty("username", "johndoe");
        newUser.addProperty("email", "johndoe@example.com");
        System.out.println(createUser(newUser));

        JsonObject updatedUser = new JsonObject();
        updatedUser.addProperty("name", "Jane Doe");
        updatedUser.addProperty("username", "janedoe");
        updatedUser.addProperty("email", "janedoe@example.com");
        System.out.println(updateUser(1, updatedUser));

        System.out.println(deleteUser(1));

        System.out.println(getAllUsers());

        System.out.println(getUserById(1));

        System.out.println(getUserByUsername("Bret"));

        System.out.println(getCommentsOfLastPostByUserId(1));

        writeCommentsOfLastPostByUserIdToFile(1);

        System.out.println(getOpenTodosByUserId(1));
    }

    public static JsonObject createUser(JsonObject user) throws IOException {
        String url = BASE_URL + "/users";
        String response = sendPostRequest(url, user.toString());
        return gson.fromJson(response, JsonObject.class);
    }

    public static JsonObject updateUser(int id, JsonObject user) throws IOException {
        String url = BASE_URL + "/users/" + id;
        String response = sendPutRequest(url, user.toString());
        return gson.fromJson(response, JsonObject.class);
    }

    public static boolean deleteUser(int id) throws IOException {
        String url = BASE_URL + "/users/" + id;
        int responseCode = sendDeleteRequest(url);
        return responseCode >= 200 && responseCode < 300;
    }

    public static List<JsonObject> getAllUsers() throws IOException {
        String url = BASE_URL + "/users";
        String response = sendGetRequest(url);
        JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
        List<JsonObject> users = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            users.add(jsonElement.getAsJsonObject());
        }
        return users;
    }

    public static JsonObject getUserById(int id) throws IOException {
        String url = BASE_URL + "/users/" + id;
        String response = sendGetRequest(url);
        return gson.fromJson(response, JsonObject.class);
    }

    public static List<JsonObject> getUserByUsername(String username) throws IOException {
        String url = BASE_URL + "/users?username=" + username;
        String response = sendGetRequest(url);
        JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
        List<JsonObject> users = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            users.add(jsonElement.getAsJsonObject());
        }
        return users;
    }

    public static List<JsonObject> getCommentsOfLastPostByUserId(int userId) throws IOException {
        List<JsonObject> posts = getPostsByUserId(userId);
        int maxId = -1;
        for (JsonObject post : posts) {
            int id = post.get("id").getAsInt();
            if (id > maxId) {
                maxId = id;
            }
        }

        if (maxId == -1) {
            return new ArrayList<>();
        }

        return getCommentsByPostId(maxId);
    }

    public static void writeCommentsOfLastPostByUserIdToFile(int userId) throws IOException {
        List<JsonObject> posts = getPostsByUserId(userId);
        int maxId = -1;
        for (JsonObject post : posts) {
            int id = post.get("id").getAsInt();
            if (id > maxId) {
                maxId = id;
            }
        }

        if (maxId == -1) {
            return;
        }

        List<JsonObject> comments = getCommentsByPostId(maxId);
        String fileName = "user-" + userId + "-post-" + maxId + "-comments.json";
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(comments, writer);
        }
    }

    public static List<JsonObject> getOpenTodosByUserId(int userId) throws IOException {
        List<JsonObject> todos = getTodosByUserId(userId);
        List<JsonObject> openTodos = new ArrayList<>();
        for (JsonObject todo : todos) {
            if (!todo.get("completed").getAsBoolean()) {
                openTodos.add(todo);
            }
        }
        return openTodos;
    }

    private static List<JsonObject> getPostsByUserId(int userId) throws IOException {
        String url = BASE_URL + "/users/" + userId + "/posts";
        String response = sendGetRequest(url);
        JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
        List<JsonObject> posts = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            posts.add(jsonElement.getAsJsonObject());
        }
        return posts;
    }

    private static List<JsonObject> getCommentsByPostId(int postId) throws IOException {
        String url = BASE_URL + "/posts/" + postId + "/comments";
        String response = sendGetRequest(url);
        JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
        List<JsonObject> comments = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            comments.add(jsonElement.getAsJsonObject());
        }
        return comments;
    }

    private static List<JsonObject> getTodosByUserId(int userId) throws IOException {
        String url = BASE_URL + "/users/" + userId + "/todos";
        String response = sendGetRequest(url);
        JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
        List<JsonObject> todos = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            todos.add(jsonElement.getAsJsonObject());
        }
        return todos;
    }

    private static String sendGetRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("Failed to send GET request: " + responseCode);
        }
    }

    private static String sendPostRequest(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("Failed to send POST request: " + responseCode);
        }
    }

    private static String sendPutRequest(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("PUT");

        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();

        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("Failed to send PUT request: " + responseCode);
        }
    }

    private static int sendDeleteRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("DELETE");

        return con.getResponseCode();
    }

}
