package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuickNestCujRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testBottomNavigationAndTabs() {
        composeTestRule.waitForIdle()

        // If onboarding is displayed, skip through to main app
        if (composeTestRule.onAllNodesWithTag("onboarding_skip_button").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeTestRule.waitForIdle()
        }

        // If login screen is displayed, continue as guest
        if (composeTestRule.onAllNodesWithTag("guest_login_button").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("guest_login_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
        }

        // 1. Verify Bottom Navigation Bar is displayed
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()

        // 2. Verify Home FAB "Sell Fast" is displayed on Home tab
        composeTestRule.onNodeWithTag("fab_sell_fast").assertIsDisplayed()

        // 3. Click "Explore" tab
        composeTestRule.onNodeWithTag("nav_explore").performClick()
        composeTestRule.waitForIdle()

        // 4. Click "Saved" tab
        composeTestRule.onNodeWithTag("nav_saved").performClick()
        composeTestRule.waitForIdle()

        // 5. Click "Profile" tab
        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.waitForIdle()

        // 6. Return back to "Home" tab
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()

        // 7. Verify FAB is shown again on Home tab
        composeTestRule.onNodeWithTag("fab_sell_fast").assertIsDisplayed()
    }
}
