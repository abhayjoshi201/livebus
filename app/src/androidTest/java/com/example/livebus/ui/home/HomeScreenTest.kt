package com.example.livebus.ui.home

import com.example.livebus.ui.theme.*
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_isInteractive() {
        var searchClicked = false
        composeTestRule.setContent {
            HomeScreen(onSearchClick = { searchClicked = true })
        }
        composeTestRule.onNodeWithText("Search routes or stops...").performClick()
        assert(searchClicked)
    }

    @Test
    fun favoriteRoutesSection_displaysRoutes() {
        composeTestRule.setContent {
            HomeScreen()
        }
        composeTestRule.onNodeWithText("Suggested Routes").assertExists()
        composeTestRule.onAllNodesWithText("ROUTE D-1").assertCountEquals(1)
        composeTestRule.onNodeWithText("ROUTE D-2").assertExists()
        composeTestRule.onAllNodesWithText("Clement Town Ca").assertCountEquals(2)
    }

    @Test
    fun stopsSections_displayCorrectly() {
        composeTestRule.setContent {
            HomeScreen()
        }
        composeTestRule.onNodeWithText("Pinned Stops").assertExists()
        composeTestRule.onNodeWithText("Nearest Stops").assertExists()
    }

    @Test
    fun bottomNavigationBar_itemsAreClickable() {
        composeTestRule.setContent {
            HomeScreen()
        }
        composeTestRule.onNodeWithText("Home").assertExists().performClick()
        composeTestRule.onNodeWithText("Map").assertExists().performClick()
        composeTestRule.onNodeWithText("Tickets").assertExists().performClick()
        composeTestRule.onNodeWithText("Alerts").assertExists().performClick()
        composeTestRule.onNodeWithText("Settings").assertExists().performClick()
    }
}
