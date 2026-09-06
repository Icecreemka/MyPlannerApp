@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.uliana.myplanner.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val titleResId: Int,
    val descResId: Int
)

private val pages = listOf(
    OnboardingPage(Icons.Filled.Park, R.string.onboarding_1_title, R.string.onboarding_1_desc),
    OnboardingPage(Icons.Filled.Spa, R.string.onboarding_2_title, R.string.onboarding_2_desc),
    OnboardingPage(Icons.Filled.Grass, R.string.onboarding_3_title, R.string.onboarding_3_desc),
    OnboardingPage(Icons.Filled.Schedule, R.string.onboarding_4_title, R.string.onboarding_4_desc),
    OnboardingPage(Icons.Filled.CalendarMonth, R.string.onboarding_5_title, R.string.onboarding_5_desc),
    OnboardingPage(Icons.Filled.Repeat, R.string.onboarding_6_title, R.string.onboarding_6_desc),
    OnboardingPage(Icons.Filled.AutoAwesome, R.string.onboarding_7_title, R.string.onboarding_7_desc),
    OnboardingPage(Icons.Filled.DragIndicator, R.string.onboarding_8_title, R.string.onboarding_8_desc),
    OnboardingPage(Icons.Filled.NotificationsActive, R.string.onboarding_9_title, R.string.onboarding_9_desc),
    OnboardingPage(Icons.Filled.Eco, R.string.onboarding_10_title, R.string.onboarding_10_desc),
    OnboardingPage(Icons.Filled.Savings, R.string.onboarding_11_title, R.string.onboarding_11_desc),
    OnboardingPage(Icons.Filled.QuestionMark, R.string.onboarding_12_title, R.string.onboarding_12_desc)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingOverlay(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) { Text(stringResource(R.string.onboarding_skip)) }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = stringResource(page.titleResId),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(page.descResId),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val isLastPage = pagerState.currentPage == pages.lastIndex
                Button(onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }) {
                    Text(stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next))
                }
            }
        }
    }
}
