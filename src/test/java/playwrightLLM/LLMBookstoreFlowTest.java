package playwrightLLM;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class LLMBookstoreFlowTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void afterAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/llm"))
                .setRecordVideoSize(1280, 720));
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void llmGeneratedFlow() {
        // Example of a simpler, LLM-assisted test:
        page.navigate("https://depaul.bncollege.com/");
        page.waitForLoadState();

        page.getByPlaceholder("Enter your search details").fill("earbuds");
        page.keyboard().press("Enter");
        page.waitForLoadState();

        // Assert we see some JBL earbuds result (less strict than manual test)
        assertTrue(
                page.getByText("JBL", new Page.GetByTextOptions().setExact(false))
                        .first().isVisible()
        );
    }
}

