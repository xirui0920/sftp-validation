package com.example.sftp;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import java.util.Properties;

/**
 * author: Cyrus
 * date: 2019/9/30 10:34
 * project: validation
 */
@Slf4j
@SpringBootApplication
public class Application implements ApplicationRunner {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("help") || args.getOptionNames().isEmpty()) {
            System.out.println("             本工具用于验证JAVA连接SFTP               ");
            System.out.println("                                                    ");
            System.out.println("                   版本V0.0.1                        ");
            System.out.println("                                                    ");
            System.out.println("--------------------参数说明-------------------------");
            System.out.println("--host"+"                     :FTP地址 默认是localhost");
            System.out.println("--port"+"                     :FTP端口");
            System.out.println("--username"+"                 :FTP用户名");
            System.out.println("--password"+"                 :FTP密码");
            System.out.println("--privateKey"+"               :ssh秘钥");
            System.out.println("--strictHostKeyChecking"+"    :是否开启强验证 yes or no");
            System.out.println("--path"+"                     :要ls的路径");
            return;
        }
        ChannelSftp sftp;
        Session session;
        String username = null;
        String password = null;
        String privateKey = null;
        String host = "localhost";
        String strictHostKeyChecking = "yes";
        String path = "/";
        int port = 22;
        if (args.containsOption("username")) {
            username = args.getOptionValues("username").get(0);
        }
        if (args.containsOption("password")) {
            password = args.getOptionValues("password").get(0);
        }
        if (args.containsOption("privateKey")) {
            privateKey = args.getOptionValues("privateKey").get(0);
        }
        if (args.containsOption("host")) {
            host = args.getOptionValues("host").get(0);
        }
        if (args.containsOption("port")) {
            port = Integer.parseInt(args.getOptionValues("port").get(0));
        }
        if (args.containsOption("strictHostKeyChecking")) {
            strictHostKeyChecking = args.getOptionValues("strictHostKeyChecking").get(0);
        }
        if (args.containsOption("path")) {
            path = args.getOptionValues("path").get(0);
        }
        try {
            JSch jSch = new JSch();
            if (!StringUtils.isEmpty(privateKey)) {
                jSch.addIdentity(privateKey);
                log.info("sftp connect,path of private key file：{}" , privateKey);
            }
            log.info("sftp connect by host:{} port:{} username:{}", host, port, username);
            session = jSch.getSession(username, host, port);
            log.info("session is build.");
            if (!StringUtils.isEmpty(password)) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", strictHostKeyChecking);

            session.setConfig(config);
            session.connect();
            log.info("session is connected.");

            Channel channel = session.openChannel("sftp");
            channel.connect();
            log.info("channel is connected.");

            sftp = (ChannelSftp)channel;
        } catch (JSchException e) {
            log.error("Cannot connect to specified sftp server : {}:{}", host, port);
            log.error(e.getMessage());
            log.error(toStackTrace(e));
            return;
        }

        sftp.cd(path);
        log.info(sftp.lpwd());
        Object[] objects = sftp.ls(path).toArray();
        for (Object object : objects) {
            log.info(object.toString());
        }

        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
                log.info("sftp is closed already.");
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
                log.info("session is closed already.");
            }
        }
    }

    private String toStackTrace(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String s = "";
        for (int i = 0; i < stackTraceElements.length; i++) {
            s = s.concat("\n"+stackTraceElements[i].toString());
        }
        return s;
    }
}
