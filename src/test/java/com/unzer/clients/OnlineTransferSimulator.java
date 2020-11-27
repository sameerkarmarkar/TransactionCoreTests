package com.unzer.clients;

import com.unzer.constants.PaymentNetworkProvider;
import com.unzer.constants.TransactionCode;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import java.util.concurrent.TimeUnit;
@Slf4j
public class OnlineTransferSimulator {

    private static WebDriver driver;

    public static void authorizeOnlineTransfer(ResponseType response, PaymentNetworkProvider pnp) {
        if (response.getTransaction().getPayment().getCode().contains(TransactionCode.PREAUTHORIZATION.getCode())
                && null != response.getTransaction().getProcessing().getRedirect()
                && null != response.getTransaction().getProcessing().getRedirect().getUrl()) {
            driver = new HtmlUnitDriver(true);
            driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
            String redirectUrl = response.getTransaction().getProcessing().getRedirect().getUrl();

            switch (pnp) {
                case PPRO:
                    authorizePproOnlineTransfer(redirectUrl);
                    break;
                case SOFORT:
                    authorizeSofortOnlineTransfer(redirectUrl);
                    break;
            }

            driver.close();
            driver.quit();
        }

    }

    private static void authorizePproOnlineTransfer(String redirectUrl) {
        driver.get(redirectUrl);
        driver.findElement(By.xpath("//button[contains(text(),'Login')]")).click();
        driver.findElement(By.xpath("//button[contains(text(),'Make Payment')]")).click();
        driver.findElement(By.xpath("//button[contains(text(),'Back to where you came from')]")).click();

        log.info("Online Payment Authorization successful");
    }

    private static void authorizeSofortOnlineTransfer(String redirectUrl) {
        driver.get(redirectUrl);
        driver.findElement(By.id("BankCodeSearch")).clear();
        driver.findElement(By.id("BankCodeSearch")).sendKeys("Demo");
        driver.findElement(By.xpath("//a[contains(@title,'Demo Bank')]")).click();
        driver.findElement(By.id("BackendFormLOGINNAMEUSERID")).clear();
        driver.findElement(By.id("BackendFormLOGINNAMEUSERID")).sendKeys("12345679");
        driver.findElement(By.id("BackendFormUSERPIN")).clear();
        driver.findElement(By.id("BackendFormUSERPIN")).sendKeys("123456");
        driver.findElement(By.xpath("//button[contains(text(), 'Weiter')]")).click();
        driver.findElement(By.xpath("//button[contains(text(), 'Weiter')]")).click();
        driver.findElement(By.id("BackendFormTan")).sendKeys("12345");
        driver.findElement(By.xpath("//button[contains(text(), 'Weiter')]")).click();
        log.info("Online Payment Authorization successful");

    }
}
