package tech.zerofiltre.testing.calcul.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.Duration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MultiplicationJourneyE2E {

  @LocalServerPort
  private int port;

  private WebDriver webDriver;
  private String baseUrl;

  @BeforeAll
  static void setUpChromeDriver() {
    WebDriverManager.chromedriver().setup();
  }

  @BeforeEach
  void setUpWebDriver() {
    webDriver = new ChromeDriver();
    baseUrl = "http://localhost:" + port + "/calculator";
  }

  @AfterEach
  void quitWebDriver() {
    if (webDriver != null) {
      webDriver.quit();
    }
  }

  @Test
  void multiplyTwoBySixteenMustReturn32() {

    webDriver.get(baseUrl);

    WebElement leftField = webDriver.findElement(By.id("left"));
    WebElement typeDropDown = webDriver.findElement(By.id("type"));
    WebElement rightField = webDriver.findElement(By.id("right"));
    WebElement submitButton = webDriver.findElement(By.id("submit"));

    leftField.sendKeys("2");
    typeDropDown.sendKeys("x");
    rightField.sendKeys("16");
    submitButton.click();

    WebDriverWait waiter = new WebDriverWait(webDriver, Duration.ofSeconds(5));

    WebElement solutionElement =
            waiter.until(ExpectedConditions.presenceOfElementLocated(By.id("solution")));

    assertThat(solutionElement.getText()).isEqualTo("32");
  }
}