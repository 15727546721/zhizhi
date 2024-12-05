package cn.xu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@Configurable
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(Application.class);
        Environment env = application.getEnvironment();
        String ip = "localhost";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException e){
            log.error("获取ip地址失败", e);
        }
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        path = path == null ? "" : path;
        System.out.println("\n----------------------------------------------------------\n\t" +
                "blog is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Knife4j-ui: \thttp://" + ip + ":" + port + path + "/doc.html\n\t" +
                "----------------------------------------------------------");
    }

}
