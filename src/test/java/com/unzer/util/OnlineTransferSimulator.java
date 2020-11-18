package com.unzer.util;

import com.unzer.constants.TransactionCode;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import java.util.concurrent.TimeUnit;
@Slf4j
public class OnlineTransferSimulator {

    private static final WebDriver driver = new HtmlUnitDriver(true);

    public static void authorizeOnlineTransfer(ResponseType response) {
        authorizePproOnlineTransfer(response);

    }

    private static void authorizePproOnlineTransfer(ResponseType response) {
        if(response.getTransaction().getPayment().getCode().contains(TransactionCode.PREAUTHORIZATION.getCode())) {
            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            String redirectUrl = response.getTransaction().getProcessing().getRedirect().getUrl();
            driver.get(redirectUrl);
            log.info(driver.getPageSource());
            driver.findElement(By.xpath("//button[contains(text(),'Login')]")).click();
            driver.findElement(By.xpath("//button[contains(text(),'Make Payment')]")).click();
            driver.findElement(By.xpath("//button[contains(text(),'Back to where you came from')]")).click();
            driver.close();
            driver.quit();
        }

        log.info("Online Payment Authorization successful");
    }

    private static void authorizeSofortOnlineTransfer() {

    }
}
