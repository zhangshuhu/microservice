package org.study.common.util.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

public class ShutdownHook {
    private Logger logger = LoggerFactory.getLogger(ShutdownHook.class);
    private long dubboShutdownTimeOutMills = 10000;//dubbo框架关闭的超时时间
    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    public ShutdownHook() {
    }

    @PostConstruct
    public void init() {
        registerShutdownHook();
    }

    private void registerShutdownHook(){
        Thread shutdownHook = new Thread() {
            public void run() {
                try {
                    long timeOut = dubboShutdownTimeOutMills;
                    logger.info("Waiting for Dubbo shutdown with {} seconds", ((int)(timeOut/1000)));
                    Thread.sleep(timeOut);
                } catch (Throwable e) {
                    logger.error("Exception Happen While Dubbo shutting down", e);
                }

                try {
                    //增加额外的3秒休眠 是为了让Dubbo更彻底的不接收外部请求
                    Thread.sleep(3000);
                    logger.info("Dubbo is closed, start to exit SpringApplication");
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }

                try {
                    SpringApplication.exit(ShutdownHook.this.configurableApplicationContext);
                    logger.info("SpringApplication Exited, Application shutdown");
                } finally {
                    removeShutdownHook(this);
                }
            }
        };

        logger.info("Register shutdownHook to JVM, threadId: {} threadName: {}", shutdownHook.getId(), shutdownHook.getName());
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void removeShutdownHook(Thread thread){
        logger.info("Remove shutdownHook from JVM, threadId: {} threadName: {}", thread.getId(), thread.getName());
        try{
            Runtime.getRuntime().removeShutdownHook(thread);
        }catch (IllegalStateException e){
        }
    }

    public long getDubboShutdownTimeOutMills() {
        return dubboShutdownTimeOutMills;
    }

    public void setDubboShutdownTimeOutMills(long dubboShutdownTimeOutMills) {
        this.dubboShutdownTimeOutMills = dubboShutdownTimeOutMills;
    }
}
