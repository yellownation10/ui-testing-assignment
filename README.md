# SE333 – Assignment 6: UI Testing with Playwright

This project contains UI tests for the DePaul Bookstore website using **Playwright for Java** and **JUnit 5**.

## Project Structure

```text
ui-testing-assignment/
  pom.xml
  src/
    test/
      java/
        playwrightTraditional/
          BookstoreFlowTest.java
        playwrightLLM/
          LLMBookstoreFlowTest.java
(Best-effort) applies Brand / Color / Price filters

Opens the JBL Quantum earbuds product page

Adds item to cart

Verifies cart contents (robust / conditional checks to handle live-site changes)

Proceeds through checkout steps (Create Account / Contact Info / Pickup / Payment) when present

Optionally empties the cart at the end

Records a video of each test run (see below)

2. playwrightLLM.LLMBookstoreFlowTest

Implements the same flow using code inspired by / generated via an LLM:

How to Run

From the project root:

mvn -Dplaywright.cli.install=true test


This will:

Install browsers (if needed)

Run both LLMBookstoreFlowTest and BookstoreFlowTest

To run a single test class:

mvn -Dtest=playwrightTraditional.BookstoreFlowTest test
mvn -Dtest=playwrightLLM.LLMBookstoreFlowTest test





Uses Playwright to drive the browser for the JBL earbuds scenario

Serves as a comparison against the traditional test in terms of implementation style    
