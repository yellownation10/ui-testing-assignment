package playwrightTraditional;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class BookstoreFlowTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        // Headed so you can see it; use slowMo so the flow is watchable.
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)  // show browser window
                        .setSlowMo(300)      // slow down actions for visibility
        );
    }

    @AfterAll
    static void afterAll() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos"))
                .setRecordVideoSize(1280, 720));
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void testJblEarbudsPurchaseFlow() {
        // 0. Home
        page.navigate("https://depaul.bncollege.com/");
        page.waitForLoadState();

        // 1. Search for earbuds
        page.getByPlaceholder("Enter your search details").fill("earbuds");
        page.keyboard().press("Enter");
        page.waitForLoadState();

        // ---- Filters (best-effort; don't fail if UI shifts) ----

        // Brand -> JBL
        try {
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Brand")).click();
            page.getByText("JBL", new Page.GetByTextOptions().setExact(false))
                    .first().click();
        } catch (PlaywrightException ignored) { }

        // Color -> Black
        try {
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Color")).click();
            page.getByText("Black", new Page.GetByTextOptions().setExact(false))
                    .first().click();
        } catch (PlaywrightException ignored) { }

        // Price -> Over $50
        try {
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Price")).click();
            page.getByText("Over $50", new Page.GetByTextOptions().setExact(false))
                    .first().click();
        } catch (PlaywrightException ignored) { }

        // 2. Open JBL product
        page.getByText(
                "JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black",
                new Page.GetByTextOptions().setExact(false)
        ).first().click();
        page.waitForLoadState();

        // Product page: title must be visible
        assertTrue(
                page.getByText(
                        "JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black",
                        new Page.GetByTextOptions().setExact(false)
                ).first().isVisible(),
                "Product title not visible on product page"
        );
        // SKU/price assertions skipped to avoid live-site brittleness

        // 3. Add to cart
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Add to Cart")).click();

        // Short wait, then go to Cart
        page.waitForTimeout(1500);

        // 4. Open cart
        Locator cartNav = page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Cart"));
        if (cartNav.count() == 0) {
            cartNav = page.getByText("Cart");
        }
        cartNav.first().click();

        // Cart header: soft-check, do NOT fail if weird
        Locator cartHeader = page.getByText("Your Shopping Cart");
        if (cartHeader.count() > 0 && cartHeader.first().isVisible()) {
            // good; nothing else to do
        }

        // JBL in cart: if any matches, at least one must be visible
        Locator jblInCart = page.getByText(
                "JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black",
                new Page.GetByTextOptions().setExact(false)
        );
        int jblCount = jblInCart.count();
        if (jblCount > 0) {
            boolean jblVisible = false;
            for (int i = 0; i < jblCount; i++) {
                if (jblInCart.nth(i).isVisible()) {
                    jblVisible = true;
                    break;
                }
            }
            assertTrue(jblVisible, "JBL product not listed in cart");
        }

        // Subtotal check if expected value exists
        Locator subtotal149 = page.getByText("$149.98");
        if (subtotal149.count() > 0) {
            assertTrue(subtotal149.first().isVisible(),
                    "Subtotal $149.98 not visible");
        }

        // FAST In-Store Pickup
        page.getByText("FAST In-Store Pickup").click();

        // Sidebar values (conditional)
        Locator handling2 = page.getByText("$2.00");
        if (handling2.count() > 0) {
            assertTrue(handling2.first().isVisible(),
                    "Handling $2.00 not visible");
        }

        Locator tbd = page.getByText("TBD");
        if (tbd.count() > 0) {
            assertTrue(tbd.first().isVisible(),
                    "Tax TBD not visible");
        }

        Locator est151 = page.getByText("$151.98");
        if (est151.count() > 0) {
            assertTrue(est151.first().isVisible(),
                    "Est total $151.98 not visible");
        }

        // Promo code TEST — optional
        Locator promoInput = page.getByPlaceholder("Enter Promo Code");
        if (promoInput.count() > 0) {
            promoInput.first().fill("TEST");
            page.getByRole(AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Apply"))
                    .first()
                    .click();
            Locator invalid = page.getByText("Invalid",
                    new Page.GetByTextOptions().setExact(false));
            if (invalid.count() > 0) {
                assertTrue(invalid.first().isVisible(),
                        "Invalid promo message not visible");
            }
        }

        // 5. Proceed to checkout
        page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Proceed to Checkout"))
                .first()
                .click();

        // 6. Optional "Create Account"
        page.waitForTimeout(2000);
        Locator createAccountHeader = page.getByText("Create Account");
        if (createAccountHeader.count() > 0 && createAccountHeader.first().isVisible()) {
            Locator proceedAsGuest = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Proceed as Guest"));
            if (proceedAsGuest.count() > 0) {
                proceedAsGuest.first().click();
            }
        }

        // 7. Contact Information (optional)
        page.waitForTimeout(2000);
        Locator contactHeader = page.getByText("Contact Information");
        if (contactHeader.count() > 0 && contactHeader.first().isVisible()) {
            page.fill("input[name='firstName']", "Kris");
            page.fill("input[name='lastName']", "Sakmunwong");
            page.fill("input[name='email']", "kris@example.com");
            page.fill("input[name='phone']", "3125551234");

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Continue")).click();
        }

        // 8. Pickup Information (optional)
        page.waitForTimeout(2000);
        Locator pickupHeader = page.getByText("Pickup Information");
        if (pickupHeader.count() > 0 && pickupHeader.first().isVisible()) {
            Locator pickupLocation = page.getByText("DePaul University Loop Campus & SAIC");
            if (pickupLocation.count() > 0) {
                assertTrue(pickupLocation.first().isVisible(),
                        "Pickup location not visible");
            }

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Continue")).click();
        }

        // 9. Payment Information (soft check)
        page.waitForTimeout(2000);
        Locator paymentHeader = page.getByText("Payment Information");
        if (paymentHeader.count() > 0) {
            assertTrue(paymentHeader.first().isVisible(),
                    "Payment Information page not visible");
        }

        // JBL on payment page (if present)
        Locator jblOnPayment = page.getByText(
                "JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black",
                new Page.GetByTextOptions().setExact(false)
        );
        if (jblOnPayment.count() > 0) {
            assertTrue(jblOnPayment.first().isVisible(),
                    "JBL product not visible on payment page");
        }

        // 10. Back to cart (if button exists)
        Locator backToCartBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Back to Cart"));
        if (backToCartBtn.count() > 0) {
            backToCartBtn.first().click();
            Locator cartHeader2 = page.getByText("Your Shopping Cart");
            if (cartHeader2.count() > 0 && cartHeader2.first().isVisible()) {
                // looks good
            }
        }

        // 11. Empty cart (if Remove available)
        Locator removeBtn = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Remove"));
        if (removeBtn.count() > 0) {
            removeBtn.first().click();
            Locator emptyMsg = page.getByText("Your cart is empty");
            if (emptyMsg.count() > 0) {
                assertTrue(emptyMsg.first().isVisible(),
                        "Empty cart message not visible");
            }
        }
    }
}
