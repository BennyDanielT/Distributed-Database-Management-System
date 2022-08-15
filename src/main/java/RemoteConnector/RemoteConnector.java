package RemoteConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.dpg7.main.FileProcessor;
import com.jcraft.jsch.*;

public class RemoteConnector {
    ChannelSftp channelSftp = null;
    Session remoteSession = null;

    // String basePath = "/home/imaphong/csci-5408-w2022-dpg7/";
    public RemoteConnector() {
        String username = "imaphong12";
        String host = "";
        String publicKeyFile = "";
        String privateKeyFile = "";
        try (InputStream input = FileProcessor.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            // load a properties file from class path, inside static method
            prop.load(input);

            // get the property value and print it out
            host = prop.getProperty("remote_ip");
            publicKeyFile = prop.getProperty("public_key_file");
            privateKeyFile = prop.getProperty("private_key_file");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int port = 22;
        Session session = null;
        List<String> fileArr = new ArrayList<>();

        try {
            JSch jSch = new JSch();
            byte[] privateKeyBytes = Files.readAllBytes(Path.of(privateKeyFile));
            byte[] publicKeyBytes = Files.readAllBytes(Path.of(publicKeyFile));
            jSch.addIdentity(username, privateKeyBytes, publicKeyBytes, "".getBytes(StandardCharsets.UTF_8));
            session = jSch.getSession(username, host, port);
            remoteSession = session;
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(30000);
            session.connect();

            // channel = (ChannelExec) session.openChannel("exec");
            // channel.setCommand("touch lol.py");
            // ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            // channel.setOutputStream(responseStream);
            // channel.connect();
            //
            // while (channel.isConnected()) {
            // Thread.sleep(100);
            // }

            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream getInputStream(String path) throws SftpException {
        System.out.println(path);
        return channelSftp.get(path);
    }

    public void replaceFileInRemote(String filePath) throws SftpException {
        channelSftp.put(filePath, filePath);
    }


    public void closeConnection() {
        channelSftp.disconnect();
        remoteSession.disconnect();
    }
}
