package com.ikoro.android.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

 data class NavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)
