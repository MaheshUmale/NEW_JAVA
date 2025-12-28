import asyncio
from playwright.async_api import async_playwright

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        page = await browser.new_page()

        # Listen for all console events and print them
        page.on("console", lambda msg: print(f"Browser console: {msg.text}"))

        try:
            await page.goto("http://localhost:8000", timeout=10000)
            await asyncio.sleep(10)  # Wait for data to load and chart to render

            await page.screenshot(path="dashboard/ui_snapshot.png")
            print("Screenshot saved to dashboard/ui_snapshot.png")
        except Exception as e:
            print(f"An error occurred: {e}")
        finally:
            await browser.close()

if __name__ == "__main__":
    asyncio.run(main())
